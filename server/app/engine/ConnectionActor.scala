package engine

import akka.actor.{ActorRef, IO, IOManager, Actor}
import akka.actor.Status.{Failure, Success}
import akka.util.ByteString
import akka.actor.IO.{SocketHandle}
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Input.{El, EOF}
import java.util.concurrent.{LinkedBlockingQueue, LinkedBlockingDeque}
import io.BytePickle.Ref
import java.util.concurrent.atomic.AtomicReference


object ConnectionActor {
  case class Connect()

  case class Disconnect()

  case class Write(bytes:Array[Byte])

  case class AwaitRead()
}

class SimpleAtomicRef[A] {
  var ref = new AtomicReference[A]

  def apply() = ref.get()
  def update(value:A) = ref.set(value)

  def map[B](f : A => B) : Option[B] = {
    val current = ref.get()
    if (current != null ) Some(f(current)) else None
  }

  def mapAndClear[B](f : A => B) : Option[B] = {
    val current = ref.getAndSet(null.asInstanceOf[A])
    if (current != null ) Some(f(current)) else None
  }
}

class ConnectionActor(val host: String, val port: Int, val downQueue: DownStreamQueue) extends Actor {

  import ConnectionActor._

  var socket: SocketHandle = null
  var connectReceiver = new SimpleAtomicRef[ActorRef]
  var readReceiver = new SimpleAtomicRef[ActorRef]

  override protected def receive = {
    case Connect =>
      connectReceiver() = sender
      socket = IOManager(context.system).connect(host, port)

    case Disconnect =>
      socket.close()
      context.stop(self)

    case Write(bytes) =>
      socket.write(ByteString(bytes))

    case AwaitRead =>
      readReceiver() = sender
      if (downQueue.available > 0 )
        readReceiver.mapAndClear(_ ! Success)


    case IO.Connected(socket, address) =>
      connectReceiver.mapAndClear(_ ! Success)

    case IO.Closed(handle, causeOpt) =>
      connectReceiver.mapAndClear(_ ! causeOpt.map(Failure(_)).getOrElse(Failure(new RuntimeException)))
      downQueue.send(EOF)
      readReceiver.mapAndClear(_ ! Success)

    case IO.Read(socket, bytes) =>
      downQueue.send(El(bytes))
      readReceiver.mapAndClear(_ ! Success)

  }
}
