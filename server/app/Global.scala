import engine.{ConnectionTable, ThreadPing}
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api.Application
import play.api.GlobalSettings

import play.api.mvc.RequestHeader
import play.api.Play.current
import akka.util.duration._
import play.api.mvc.Results._

object Global extends GlobalSettings {
  override def onStart(app: Application) {

    Logger.info("Application has started")

    Akka.system.scheduler.schedule(10 seconds, 10 seconds) {
      Logger.info("Sweep")
      ThreadPing.pingAll
      println("Remaining")
      ConnectionTable.foreach {
        case (id, extConn ) =>
          println("  " + id)
      }
    }
  }

  override def onStop(app: Application) {

    Logger.info("Application shutdown...")
  }

}
