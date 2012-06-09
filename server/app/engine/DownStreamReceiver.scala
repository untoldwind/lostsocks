package engine

import java.util.concurrent.LinkedBlockingQueue
import akka.util.ByteString
import collection.mutable.ArrayBuffer
import scala.Unit
import play.api.libs.iteratee._

trait DownStreamReceiver {
  def send(data: Input[ByteString])
}

class QueuedDownStreamReceiver extends DownStreamReceiver {

  val queue = new LinkedBlockingQueue[Input[ByteString]]

  def send(data: Input[ByteString]) = queue.offer(data)

  def available = queue.size()

  def take = queue.take()
}

class EnumeratorDownStreamReceiver extends DownStreamReceiver {
  val stored = new ArrayBuffer[Input[ByteString]]

  var currentIteratee: Iteratee[Array[Byte], Unit] = null

  def send(data: Input[ByteString]) = synchronized {
    println(">>>>>>>>>>>>>>>> send" + data)
    if (currentIteratee != null) {
      println(">> sending to iteratee")
      if (!stored.isEmpty) {
        println(">>> draining stored1 " + stored.size)
        stored.foreach(writeTo(_))
        stored.clear
        println(">>> draining stored1a " + stored.size)
      }
      writeTo(data)
    } else {
      println(">> storing")
      stored += data
    }
  }

  def clearIteratee = synchronized {
    println(">>> clear iteratee")
    currentIteratee = null
  }

  def assignIteratee(iteratee: Iteratee[Array[Byte], Unit]) = synchronized {
    println(">>> assign iteratee")
    currentIteratee = iteratee
    if (!stored.isEmpty) {
      println(">>> draining stored2 " + stored.size)
      stored.foreach(writeTo(_))
      stored.clear
      println(">>> draining stored3 " + stored.size)
    }
  }

  def writeTo(data: Input[ByteString]) = {
    println(">>> writety" + data);
    currentIteratee = currentIteratee.pureFlatFold(
      // DONE
      (a, in) => {
        clearIteratee
        Done(a, in)
      },
      // CONTINUE
      k => {
        val next = k(data.map(_.toArray))
        next.pureFlatFold(
          (a, in) => {
            clearIteratee
            next
          },
          _ => next,
          (_, _) => next)
      },
      // ERROR
      (e, in) => {
        clearIteratee
        Error(e, in)
      })
  }


}
