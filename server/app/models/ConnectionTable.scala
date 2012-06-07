package models

import models.UserInfo
import collection.mutable.{SynchronizedMap, HashMap}
import engine.ExtendedConnection

class ConnectionTable(val userId: Long,
                      val username: String) extends HashMap[String, ExtendedConnection] with SynchronizedMap[String, ExtendedConnection] {

}

object ConnectionTable {
  val tablesByUser = new HashMap[Long, ConnectionTable] with SynchronizedMap[Long, ConnectionTable]

  def apply(userInfo: UserInfo) = tablesByUser.getOrElseUpdate(userInfo.id, {
    new ConnectionTable(userInfo.id, userInfo.username)
  })

  def foreach[U](f: ConnectionTable => U) = tablesByUser.values.foreach(f)

  def findForUser(user: UserInfo) = {
    tablesByUser.get(user.id).map {
      table => Seq(table)
    }.getOrElse(Seq.empty)
  }

  def findAll = tablesByUser.values.toSeq
}
