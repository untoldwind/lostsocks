package controllers

import java.io._

import scala.xml._
import scala.math.min
import play.api._
import http.{ContentTypeOf, Writeable}
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Input._
import play.api.libs.iteratee.Parsing._
import play.api.libs.Files.{TemporaryFile}
import com.objectcode.lostsocks.common.Constants
import java.util.zip.{GZIPOutputStream, GZIPInputStream}
import java.net.InetAddress
import com.objectcode.lostsocks.common.util.StringUtils
import models.IdGenerator
import com.objectcode.lostsocks.common.net.{Connection, DataPacket}
import engine.{ConnectionTable, ExtendedConnection}

object Connections extends Controller {
  val serverTimeout = 600
  def index = Action {
    Ok("Bla")
  }

  def create = Action(javaDataPaket) {
    implicit request =>
      request.body.`type` match {
        case Constants.CONNECTION_VERSION_REQUEST => versionRequest
        case Constants.CONNECTION_CREATE => connectionCreate
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

  def connectionCreate(implicit request: Request[DataPacket]) = {
    val input = request.body
    var output = new DataPacket()
    output.id = input.id

    val ip = request.host
    val iprev = nslookup(ip)
    val userpass = request.body.id.split(':')
    val login = userpass(0)
    val pass = userpass(1)
    val sUserTimeout = userpass(2)
    var userTimeout = sUserTimeout.toInt
    if (userTimeout < 0) userTimeout = 0

    val  id_conn = IdGenerator.generateId(login)

    Logger.info("Connection create : " + id_conn)

    val url = new String(input.tab)
    println(url)
    val host = url.substring(0, url.indexOf(':'))
    val port =  url.substring(1 + url.indexOf(':'), url.length()).toInt

    val conn = new Connection(Connection.CONNECTION_CLIENT_TYPE)
    if (conn.connect(host, port) != 0) {
      Logger.warn("Connection " + id_conn + " failed from " + iprev + "(" + ip + ") to " + host + ":" + port)
      output.`type` = Constants.CONNECTION_CREATE_KO
      val err = "Server was unable to connect to " + host + ":" + port
      output.tab = err.getBytes()
    } else {
      Logger.info("Connection " + id_conn + " created from " + iprev + "(" + ip + ") to " + host + ":" + port)

      val extConn = new ExtendedConnection()
      extConn.conn = conn;
      extConn.ip = ip
      extConn.iprev = iprev
      extConn.destIP = host
      extConn.destIPrev = nslookup(host)
      extConn.destPort = port
      extConn.user = null
      if (serverTimeout == 0) extConn.timeout = userTimeout
      else
      {
        if (userTimeout == 0) extConn.timeout = serverTimeout
        else extConn.timeout = min(userTimeout, serverTimeout)
      }
//      extConn.authorizedTime = userInfo.authorizedTime;

      // Add this to the ConnectionTable
      ConnectionTable.put(id_conn, extConn);

      // Build the response
      output.`type` = Constants.CONNECTION_CREATE_OK;
      output.id = id_conn;
      //output.tab = conn.read();
      //output.tab = Const.TAB_EMPTY;
      val resp = "" + conn.getSocket().getInetAddress().getHostAddress() + ":" + conn.getSocket().getPort()
      output.tab = resp.getBytes()
    }
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
            val zis = new GZIPInputStream(new ByteArrayInputStream(bytes))
            val ois = new ObjectInputStream(zis)
            val input = ois.readObject().asInstanceOf[DataPacket]
            ois.close()
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

  def nslookup(ip:String): String = {
    scala.util.control.Exception.allCatch[String].opt {
      InetAddress.getByName(ip).getHostName()
    }.getOrElse("?")
  }

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
