package controllers

import models.ConnectionTable
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Action, ResponseHeader, SimpleResult, Controller}

object Dashboard extends Controller with Secured {

  def index = Authenticated {
    implicit request =>
      if (request.user.canAdmin) {
        Ok(views.html.dashboard.index(ConnectionTable.findAll))
      }
      else {
        Ok(views.html.dashboard.index(ConnectionTable.findForUser(request.user)))
      }
  }

  def webstart = Action {
    implicit request =>
      SimpleResult(header = ResponseHeader(200, Map(CONTENT_TYPE -> "application/x-java-jnlp-file")),
                    Enumerator(views.xml.dashboard.webstart()))
  }
}