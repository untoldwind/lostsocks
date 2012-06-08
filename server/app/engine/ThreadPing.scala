package engine

import play.api.Logger
import collection.mutable.ArrayBuffer
import models.ConnectionTable

object ThreadPing {
  def sweepAll = {

    ConnectionTable.foreach {
      table =>
        val closeForTimeout = new ArrayBuffer[String]
        val closeForAuthorizedTime = new ArrayBuffer[String]

        table.foreach {
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
        closeForTimeout.foreach {
          id =>
            Logger.info("Closed connection " + id + " : Timeout reached...")
            val extConn = table.get(id)
            extConn.map(_.connectionActor ! ConnectionActor.Disconnect)
            table.remove(id)
        }
        closeForAuthorizedTime.foreach {
          id =>
            Logger.info("Closed connection " + id + " : Maximum time reached...")
            val extConn = table.get(id)
            extConn.map(_.connectionActor ! ConnectionActor.Disconnect)
            table.remove(id)
        }
    }

  }
}
