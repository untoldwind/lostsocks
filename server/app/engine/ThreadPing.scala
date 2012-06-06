package engine

import play.api.Logger
import collection.mutable.ArrayBuffer

object ThreadPing {
  def pingAll = {

    val closeForTimeout = new ArrayBuffer[String]
    val closeForAuthorizedTime = new ArrayBuffer[String]

    ConnectionTable.foreach {
      case (id, extConn) =>
        val now = new java.util.Date()

        if (now.getTime - extConn.lastAccessDate.getTime > 1000 * extConn.timeout) {
          closeForTimeout += id
        }
        else if (extConn.authorizedTime > 0 &&
          now.getTime - extConn.creationDate.getTime > 1000 * extConn.authorizedTime) {
          closeForAuthorizedTime += id
        }
    }

    closeForTimeout.foreach { id =>
      Logger.info("Closed connection " + id + " : Timeout reached...")
      val extConn = ConnectionTable.get(id)
      extConn.map(_.conn.disconnect)
      ConnectionTable.remove(id)
    }
    closeForAuthorizedTime.foreach { id =>
      Logger.info("Closed connection " + id + " : Maximum time reached...")
      val extConn = ConnectionTable.get(id)
      extConn.map(_.conn.disconnect)
      ConnectionTable.remove(id)
    }
  }
}
