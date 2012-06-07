package controllers

import com.objectcode.lostsocks.common.net.DataPacket
import play.api.http.{ContentTypeOf, Writeable}
import models.CompressedPacket
import play.api.mvc.{Results, BodyParser, Action, Controller}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import play.api.libs.iteratee.{Done, Iteratee}
import play.api.libs.iteratee.Input.{El, Empty}
import play.api.{Logger, Play}


object Api extends Controller with Secured with CompressedPacketFormat {
  def SUPPORTED_CLIENT_VERSIONS = List("1.0.2")

  def versionCheck = BasicAuthenticated(compressedPacket) {
    implicit request =>
      if(SUPPORTED_CLIENT_VERSIONS.contains(request.body.asString)) {
        Logger.info("Version check - Version supported : " + request.body.asString)
        Ok(CompressedPacket(request.body.data, true))
      } else {
        Logger.info("Version check - Version no more supported : " + request.body.asString)
        NotAcceptable("Version not supported")
      }
  }

  def connectionCreate = Action(compressedPacket) {
    implicit request =>
      Ok("bla")
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
