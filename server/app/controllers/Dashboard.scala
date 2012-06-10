package controllers

import play.api.mvc.Controller
import models.ConnectionTable
import collection.mutable.ArrayBuffer
import java.io.File

object Dashboard extends Controller with Secured {

  def index = Authenticated {
    implicit request =>
      val files = new ArrayBuffer[String]
      val root = new File(".")
      files += root.getAbsolutePath
      root.listFiles().foreach {
        f =>
          files += f.getAbsolutePath
          if ( f.isDirectory ) {
            f.listFiles().foreach { sf =>
              files += sf.getAbsolutePath
              if ( sf.isDirectory ) {
                sf.listFiles().foreach { ssf =>
                  files += ssf.getAbsolutePath
                }
              }
            }
          }
      }
      if (request.user.canAdmin)
        Ok(views.html.dashboard.index(ConnectionTable.findAll, files))
      else
        Ok(views.html.dashboard.index(ConnectionTable.findForUser(request.user), files))
  }
}
