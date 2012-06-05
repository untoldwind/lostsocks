package models

import java.util.concurrent.atomic.AtomicLong

object IdGenerator {
  var id = new AtomicLong(0);

  def generateId(prefix:String) = {
    prefix + "_conn_" + id.incrementAndGet();
  }
}
