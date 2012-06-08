package engine

import java.net.Socket
import play.api.Logger
import java.io._

class Connection {
  val SO_TIMEOUT = 100
  val BUFFER_SIZE = 65536

  var host: String = null
  var port: Int = 0
  var socket: Socket = null

  var inputStream: InputStream = null
  var outputStream: OutputStream = null
  var bufferedInputStream: BufferedInputStream = null
  var bufferedOutputStream: BufferedOutputStream = null

  var connected = false

  def connect(host: String, port: Int): Boolean = {
    this.host = host
    this.port = port

    try {
      socket = new Socket(this.host, this.port)

      outputStream = socket.getOutputStream()
      inputStream = socket.getInputStream()
      bufferedOutputStream = new BufferedOutputStream(outputStream)
      bufferedInputStream = new BufferedInputStream(inputStream)
    } catch {
      case e: IOException =>
        Logger.info("Failed to open Socket " + host + ":" + port)
        return false
    }
    scala.util.control.Exception.allCatch {
      socket.setSoTimeout(SO_TIMEOUT);
    }

    connected = true

    return true
  }

  def disconnect: Boolean = synchronized {
    if (!connected)
      return true

    scala.util.control.Exception.allCatch {
      bufferedInputStream.close()
      bufferedOutputStream.flush()
      bufferedOutputStream.close()
    }

    scala.util.control.Exception.allCatch {
      socket.close()
    }
    connected = false
    return true
  }

  def read: Array[Byte] = {
    if (!connected)
      return null

    val buffer = new Array[Byte](BUFFER_SIZE)

    var len = -1
    try {
      len = bufferedInputStream.read(buffer, 0, BUFFER_SIZE)
    }
    catch {
      case e: InterruptedIOException => len = 0
    }

    if (len < 0)
      return null

    buffer.slice(0, len)
  }

  def write(toWrite: Array[Byte]): Boolean = {
    if (toWrite == null || toWrite.size == 0 || !connected) return false

    try {
      bufferedOutputStream.write(toWrite)
      bufferedOutputStream.flush()
    }
    catch {
      case e: IOException => return false
    }
    return true
  }
}
