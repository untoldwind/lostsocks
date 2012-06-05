package controllers

import java.io._

import scala.xml._

import play.api._
import http.{ContentTypeOf, Writeable}
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Input._
import play.api.libs.iteratee.Parsing._
import play.api.libs.Files.{TemporaryFile}
import com.objectcode.lostsocks.common.net.DataPacket
import com.objectcode.lostsocks.common.Constants
import java.util.zip.{GZIPOutputStream, GZIPInputStream}

object Connections extends Controller {

  def index = Action {
    Ok("Bla")
  }

  def create = Action(javaDataPaket) {
    implicit request =>
      request.body.`type` match {
        case Constants.CONNECTION_VERSION_REQUEST => versionRequest
      }
  }

  def update(id: String) = Action {
    Ok("Bla")
  }

  def versionRequest(implicit request: Request[DataPacket]) = {

    var output = new DataPacket()
    output.id = request.body.id
    // Test the version
    if (isCompatible(request.body.id)) {
      Logger.info("Version check - Version supported : " + request.body.id)
      output.`type` = Constants.CONNECTION_VERSION_RESPONSE_OK
      output.id = Constants.APPLICATION_VERSION
    }
    else {
      Logger.info("Version check - Version no more supported : " + request.body.id)
      output.`type` = Constants.CONNECTION_VERSION_RESPONSE_KO
      output.id = Constants.APPLICATION_VERSION
    }
    output.tab = Constants.TAB_EMPTY
    Ok(output)
  }

  def javaDataPaket: BodyParser[DataPacket] = javaDataPaket(parse.DEFAULT_MAX_TEXT_LENGTH)

  def javaDataPaket(maxLength: Int): BodyParser[DataPacket] =
    parse.when(
                _.contentType.exists(_ == "application/x-java-serialized-object"),
                toJavaDataPaket(maxLength),
                request =>
                  Play.maybeApplication.map(_.global.onBadRequest(request, "Expecting application/x-java-serialized"))
                    .getOrElse(Results.BadRequest)
              )

  def toJavaDataPaket(maxLength: Int): BodyParser[DataPacket] = BodyParser("javaObject, maxLength=" + maxLength) {
    request =>
      Traversable.takeUpTo[Array[Byte]](maxLength).apply(Iteratee.consume[Array[Byte]]().map {
        bytes =>
          scala.util.control.Exception.allCatch[DataPacket].either {
            println("Try")
            val zis = new GZIPInputStream(new ByteArrayInputStream(bytes));
            val ois = new ObjectInputStream(zis);
            val input = ois.readObject().asInstanceOf[DataPacket];
            ois.close();
            input
          }.left.map {
            e =>
              (Play.maybeApplication.map(_.global.onBadRequest(request, "Invalid Json"))
                .getOrElse(Results.BadRequest), bytes)
          }
      }).flatMap(Iteratee.eofOrElse(Results.EntityTooLarge))
        .flatMap {
        case Left(b) => Done(Left(b), Empty)
        case Right(it) => it.flatMap {
          case Left((r, in)) => Done(Left(r), El(in))
          case Right(json) => Done(Right(json), Empty)
        }
      }
  }

  def SUPPORTED_CLIENT_VERSIONS = List("1.0", "1.0.1")

  def isCompatible(clientVersion: String) = SUPPORTED_CLIENT_VERSIONS.contains(clientVersion)

  implicit def writeableOf_DataPacket: Writeable[DataPacket] = Writeable[DataPacket](dataPaket => {
    val bos = new ByteArrayOutputStream()
    val zos = new GZIPOutputStream(bos)
    val oos = new ObjectOutputStream(zos)
    oos.writeObject(dataPaket)
    oos.close()
    bos.toByteArray
  })

  implicit def contentTypeOf_DataPacket: ContentTypeOf[DataPacket] = {

    ContentTypeOf[DataPacket](Some("application/x-java-serialized-object"))
  }

}
