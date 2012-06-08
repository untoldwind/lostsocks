package engine

import akka.actor.{ActorRef, IO, IOManager, Actor}
import akka.actor.Status.{Failure, Success}
import akka.util.ByteString
import akka.actor.IO.{SocketHandle}
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Input.{El, EOF}
import java.util.concurrent.{LinkedBlockingQueue, LinkedBlockingDeque}


object ConnectionActor {
  case class Connect()

  case class Disconnect()

  case class Write(bytes:Array[Byte])
}

class ConnectionActor(val host: String, val port: Int, val downQueue:LinkedBlockingQueue[Input[ByteString]]) extends Actor {

  import ConnectionActor._

  var socket: SocketHandle = null
  var receiver: ActorRef = null

  override protected def receive = {
    case Connect =>
      receiver = sender
      socket = IOManager(context.system).connect(host, port)

    case Disconnect =>
      socket.close()
      context.stop(self)

    case Write(bytes) =>
      socket.write(ByteString(bytes))

    case IO.Connected(socket, address) =>
      if ( receiver != null )
        receiver ! Success
      receiver = null

    case IO.Closed(handle, causeOpt) =>
      if ( receiver != null )
        receiver ! causeOpt.map(Failure(_)).getOrElse(Failure(new RuntimeException))
      receiver = null
      downQueue.offer(EOF)

    case IO.Read(socket, bytes) =>
      downQueue.offer(El(bytes))
  }
}
