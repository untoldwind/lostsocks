package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.User
import play.api.mvc.Action
import play.api.mvc.AsyncResult
import play.api.libs.openid.OpenID
import play.api.libs.concurrent.Redeemed
import play.api.libs.concurrent.Thrown

object Login extends Controller {
  val loginForm = Form(
    mapping(
      "username" -> text,
      "password" -> text) {
      (username, password) => User.authenticate(username, password)
    } {
      user => Some(user.map {
        u => u.username
      }.getOrElse(""), "")
    }
      verifying("Invalid username or password", user => user.isDefined)
      transform( {
      u: Option[User] => u.get
    }, {
      u: User => Some(u)
    }))

  def index = Action {
    Ok(views.html.login.index(loginForm))
  }

  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.login.index(formWithErrors)),
        user => redirectAuthenticated(user))
  }

  def logout = Action {
    Redirect(routes.Application.index).withNewSession
  }

  private def redirectAuthenticated(user: User) = Redirect(routes.Dashboard.index)
    .withSession("userId" -> user.id.toString, "username" -> user.username, "roles" -> user.roleNames.mkString(";"))
}