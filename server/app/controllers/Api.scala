package controllers

import scala.math.min
import play.api.http.{ContentTypeOf, Writeable}
import play.api.mvc.{Results, BodyParser, Action, Controller}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import play.api.libs.iteratee.{Done, Iteratee}
import play.api.libs.iteratee.Input.{El, Empty}
import play.api.{Logger, Play}
import com.objectcode.lostsocks.common.net.{Connection, DataPacket}
import com.objectcode.lostsocks.common.Constants
import engine.ExtendedConnection
import models.{ConnectionTable, IdGenerator, CompressedPacket}
import utils.IPHelper


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

  def connectionPing(id: String) = Action(compressedPacket) {
    implicit request =>
      Ok("bla")
  }

  def connectionPong(id: String) = Action(compressedPacket) {
    implicit request =>
      Ok("bla")
  }

  def connectionRequest(id: String) = Action(compressedPacket) {
    implicit request =>
      Ok("bla")
  }

  def connectionClose(id: String) = Action(compressedPacket) {
    implicit request =>
      Ok("bla")
  }
}
