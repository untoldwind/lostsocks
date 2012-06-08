package controllers

import scala.math.min
import play.api.mvc.{Action, Controller}
import play.api.Logger
import engine.ExtendedConnection
import models.{ConnectionTable, IdGenerator, CompressedPacket}
import utils.IPHelper
import com.objectcode.lostsocks.common.net.{DataPacket, Connection}
import com.objectcode.lostsocks.common.Constants


object Api extends Controller with Secured with CompressedPacketFormat {
  val SERVER_TIMOUT = 120

  val SUPPORTED_CLIENT_VERSIONS = List("1.0.2")

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

      val conn = new Connection(Connection.CONNECTION_CLIENT_TYPE)
      if (conn.connect(host, port) != 0) {
        Logger.warn("Connection " + connectionId + " failed from " + iprev + "(" + ip + ") to " + host + ":" + port)
        MethodNotAllowed("Server was unable to connect to " + host + ":" + port)
      } else {
        Logger.info("Connection " + connectionId + " created from " + iprev + "(" + ip + ") to " + host + ":" + port)

        val extConn = new ExtendedConnection()
        extConn.conn = conn
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
      }
  }

  def connectionRequest(id: String) = BasicAuthenticated(compressedPacket) {
    implicit request =>
      ConnectionTable(request.user).get(id).map {
        extConn =>
          val lastAccessDate = extConn.lastAccessDate
          extConn.lastAccessDate = new java.util.Date()
          val conn = extConn.conn

          // Add the sended bytes
          extConn.uploadedBytes += request.body.data.size

          // write the bytes
          conn.write(request.body.data)

          // Update the upload speed
          val div = 1 + extConn.lastAccessDate.getTime - lastAccessDate.getTime
          extConn.currentUploadSpeed = request.body.data.size.toDouble / div

          // Build the response
          val buf = conn.read()
          if (buf == null) {
            Logger.info("Connection closed: " + id)
            // Remove the connection from the ConnectionTable
            ConnectionTable(request.user).remove(id)

            Ok(CompressedPacket(Array.empty[Byte], true))
          } else {
            // Add the received bytes
            extConn.downloadedBytes += buf.size

            // Update the download speed
            val div = 1 + extConn.lastAccessDate.getTime - lastAccessDate.getTime
            extConn.currentDownloadSpeed = buf.size.toDouble / div;

            Ok(CompressedPacket(buf, false));
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
          val conn = extConn.conn

          // Close it
          conn.disconnect()

          // Remove it from the ConnectionTable
          ConnectionTable(request.user).remove(id)

          // Build the response
          Ok(CompressedPacket("Destroyed", true));
      }.getOrElse {
        println("Again1")
        Logger.info("Connection already destroyed by timeout : " + id)

        Ok(CompressedPacket("Already destroyed", true));
      }
  }

}
