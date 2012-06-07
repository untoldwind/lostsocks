package controllers

import play.api.mvc.Controller

object Dashboard extends Controller with Secured {

  def index = Authenticated {
    implicit request =>
      Ok(views.html.dashboard.index())
  }
}
