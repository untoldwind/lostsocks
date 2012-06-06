package engine

import collection.mutable.{SynchronizedMap, HashMap}

object ConnectionTable {
  val table = new HashMap[String, ExtendedConnection] with SynchronizedMap[String, ExtendedConnection]

  def put(key:String, value:ExtendedConnection) = table.put(key, value)

  def get(key:String) = table.get(key)

  def remove(key:String) = table.remove(key)

  def clear = table.clear

  def isEmpty = table.isEmpty

  def size = table.size

  def foreach[U](f:((String, ExtendedConnection)) => U) = table.foreach[U](f)
}
