package controllers

import play.api.Play.current
import scala.math.min
import models.{ConnectionTable, IdGenerator, CompressedPacket}
import utils.IPHelper
import play.api.Logger
import play.api.libs.concurrent.Akka
import akka.actor.Props
import akka.util.duration._
import akka.pattern._
import akka.dispatch.Await
import akka.util.{ByteString, Timeout}
import play.api.libs.iteratee.Input.{El, EOF, Empty}
import engine.ConnectionActor.Disconnect
import engine._
import play.api.libs.concurrent._
import anorm.Success
import play.api.mvc.{Result, AnyContent, Controller}

object Api extends Controller with Secured with CompressedPacketFormat {
  val SERVER_TIMOUT = 120

  val SUPPORTED_CLIENT_VERSIONS = List("1.0.2")

  implicit val timeout = Timeout(10 seconds)

  def versionCheck = BasicAuthenticated(compressedPacket) {
    implicit request =>
      if (SUPPORTED_CLIENT_VERSIONS.contains(request.body.asString)) {
        Logger.info("Version check - Version supported : " + request.body.asString)
        Ok(CompressedPacket(request.body.data, true))
      } else {
        Logger.info("Version check - Version no more supported : " + request.body.asString)
        NotAcceptable("Version not supported")
      }
  }

  def connectionCreate = BasicAuthenticated(compressedPacket) {
    implicit request =>
      val ip = request.host
      val iprev = IPHelper.nslookup(ip)
      val connectionId = IdGenerator.generateId(request.user.username)

      Logger.info("Connection create : " + connectionId)

      val url = new String(request.body.asString)
      val parts = url.split(":")
      val host = parts(0)
      val port = parts(1).toInt
      var userTimeout = parts(2).toInt
      if (userTimeout < 0) userTimeout = 0

      val downQueue = new DownStreamQueue
      val connectionActor = Akka.system.actorOf(Props(new ConnectionActor(host, port, downQueue)))

      try {
        Await.result(connectionActor ? ConnectionActor.Connect, timeout.duration)

        Logger.info("Connection " + connectionId + " created from " + iprev + "(" + ip + ") to " + host + ":" + port)
        val extConn = new ExtendedConnection(connectionId, connectionActor, downQueue)
        extConn.ip = ip
        extConn.iprev = iprev
        extConn.destIP = host
        extConn.destIPrev = IPHelper.nslookup(host)
        extConn.destPort = port
        extConn.user = null
        if (SERVER_TIMOUT == 0) extConn.timeout = userTimeout
        else {
          if (userTimeout == 0) extConn.timeout = SERVER_TIMOUT
          else extConn.timeout = min(userTimeout, SERVER_TIMOUT)
        }
        extConn.authorizedTime = 0
        // Add this to the ConnectionTable
        ConnectionTable(request.user).put(connectionId, extConn)

        Ok(CompressedPacket(connectionId + ":" + host + ":" + port, false))
      } catch {
        case cause =>
          Logger.warn("Connection " + connectionId + " failed from " + iprev + "(" + ip + ") to " + host + ":" + port + " " + cause)
          connectionActor ! Disconnect
          MethodNotAllowed("Server was unable to connect to " + host + ":" + port + " " + cause)
      }
  }

  def connectionRequest(id: String) = BasicAuthenticated(compressedPacket) {
    implicit request =>
      ConnectionTable(request.user).get(id).map {
        extConn =>
          val lastAccessDate = extConn.lastAccessDate
          extConn.lastAccessDate = new java.util.Date()
          val connectionActor = extConn.connectionActor

          // Add the sended bytes
          extConn.uploadedBytes += request.body.data.size

          // write the bytes
          connectionActor ! ConnectionActor.Write(request.body.data)

          extConn.incrementUp(request.body.data.size)

          Ok(CompressedPacket(Array.empty[Byte], false))
      }.getOrElse(NotFound)
  }

  def connectionGet(id: String) = BasicAuthenticated {
    implicit request =>
      ConnectionTable(request.user).get(id).map {
        extConn =>
          if (extConn.downQueue.available > 0) {
            combineQueued(extConn)
          } else {
            Async {
              val future = extConn.connectionActor ? ConnectionActor.AwaitRead
              future.map(_ => combineQueued(extConn)).recover {
                case e =>
                  Ok(CompressedPacket(Array.empty[Byte], false))
              }.asPromise
            }
          }
      }.getOrElse(NotFound)
  }

  def connectionClose(id: String) = BasicAuthenticated {
    implicit request =>
      ConnectionTable(request.user).get(id).map {
        extConn =>
          val input = request.body

          Logger.info("Connection destroy : " + id)

          extConn.lastAccessDate = new java.util.Date()
          val connectionActor = extConn.connectionActor

          // Close it
          connectionActor ! ConnectionActor.Disconnect

          // Remove it from the ConnectionTable
          ConnectionTable(request.user).remove(id)

          // Build the response
          Ok(CompressedPacket("Destroyed", true))
      }.getOrElse {
        Logger.info("Connection already destroyed by timeout : " + id)

        Ok(CompressedPacket("Already destroyed", true))
      }
  }

  def combineQueued(extConn: ExtendedConnection)(implicit request: AuthenticatedRequest[AnyContent]) = {
    var data = ByteString.empty
    var hasEOF = false
    val available = extConn.downQueue.available

    for (i <- 1 to available) {
      extConn.downQueue.take match {
        case El(bytes) => data = data ++ bytes
        case EOF => hasEOF = true
        case Empty =>
      }
    }

    if (hasEOF) {
      Logger.info("Connection closed: " + extConn.connectionId)
      // Remove the connection from the ConnectionTable
      ConnectionTable(request.user).remove(extConn.connectionId)
    }

    extConn.incrementDown(data.size)

    Ok(CompressedPacket(data.toArray, hasEOF))
  }
}
