package controllers

import play.api.mvc.Controller
import models.ConnectionTable

object Dashboard extends Controller with Secured {

  def index = Authenticated {
    implicit request =>
      if ( request.user.canAdmin)
        Ok(views.html.dashboard.index(ConnectionTable.findAll))
      else
        Ok(views.html.dashboard.index(ConnectionTable.findForUser(request.user)))
  }
}
