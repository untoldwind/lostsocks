package engine

import java.util.concurrent.LinkedBlockingQueue
import akka.util.ByteString
import collection.mutable.ArrayBuffer
import scala.Unit
import play.api.libs.iteratee._

class DownStreamQueue  {

  val queue = new LinkedBlockingQueue[Input[ByteString]]

  def send(data: Input[ByteString]) = queue.offer(data)

  def available = queue.size()

  def take = queue.take()
}
