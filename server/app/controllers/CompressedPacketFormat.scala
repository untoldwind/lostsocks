package controllers

import play.api.Play
import play.api.libs.iteratee.{Done, Iteratee, Traversable}
import play.api.libs.iteratee.Input.{El, Empty}
import models.CompressedPacket
import java.util.zip.{GZIPOutputStream, GZIPInputStream}
import play.api.http.{ContentTypeOf, Writeable}
import play.api.mvc.{Controller, Results, BodyParser}
import java.io._

trait CompressedPacketFormat {
  self: Controller =>

  val MIME_TYPE = "application/x-compressed-bytes"

  def compressedPacket: BodyParser[CompressedPacket] = compressedPacket(parse.DEFAULT_MAX_TEXT_LENGTH)

  def compressedPacket(maxLength: Int): BodyParser[CompressedPacket] =
    parse.when(
      _.contentType.exists(_ == MIME_TYPE),
      toCompressedPacket(maxLength),
      request =>
        Play.maybeApplication.map(_.global.onBadRequest(request, "Expecting " + MIME_TYPE))
          .getOrElse(Results.BadRequest)
    )

  def toCompressedPacket(maxLength: Int): BodyParser[CompressedPacket] = BodyParser("compressedPacket, maxLength=" + maxLength) {
    request =>
      Traversable.takeUpTo[Array[Byte]](maxLength).apply(Iteratee.consume[Array[Byte]]().map {
        bytes =>
          scala.util.control.Exception.allCatch[CompressedPacket].either {
            val zis = new GZIPInputStream(new ByteArrayInputStream(bytes))
            val dis = new DataInputStream(zis)
            val length = dis.readInt()
            val data = new Array[Byte](length)
            dis.readFully(data)
            val endOfCommunication = dis.readBoolean()
            dis.close()
            CompressedPacket(data, endOfCommunication)
          }.left.map {
            e =>
              (Play.maybeApplication.map(_.global.onBadRequest(request, "Invalid compression"))
                .getOrElse(Results.BadRequest), bytes)
          }
      }).flatMap(Iteratee.eofOrElse(Results.EntityTooLarge))
        .flatMap {
        case Left(b) => Done(Left(b), Empty)
        case Right(it) => it.flatMap {
          case Left((r, in)) => Done(Left(r), El(in))
          case Right(compressedPacket) => Done(Right(compressedPacket), Empty)
        }
      }
  }

  implicit def writeableOf_CompressedPacket: Writeable[CompressedPacket] = Writeable[CompressedPacket](compressedPacket => {
    val bos = new ByteArrayOutputStream()
    val zos = new GZIPOutputStream(bos)
    val dos = new DataOutputStream(zos)
    dos.writeInt(compressedPacket.data.size)
    dos.write(compressedPacket.data)
    dos.writeBoolean(compressedPacket.endOfCommunication)
    dos.flush()
    dos.close()
    bos.toByteArray
  })

  implicit def contentTypeOf_CompressedPacket: ContentTypeOf[CompressedPacket] = {

    ContentTypeOf[CompressedPacket](Some(MIME_TYPE))
  }
}
