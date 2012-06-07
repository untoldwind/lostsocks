import engine.{ConnectionTable, ThreadPing}
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
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
    SessionFactory.concreteFactory = Some(() =>
      Session.create(DB.getConnection(), new PostgreSqlAdapter()))

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
