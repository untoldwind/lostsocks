package controllers.admin

import models.User
import play.api.mvc.Controller
import controllers.Secured
import play.api.data.Form
import play.api.data.Forms._

object Users extends Controller with Secured {
  private def userForm(user: User = new User) = Form(
    mapping(
      "username" -> text(minLength = 4),
      "email" -> email,
      "firstname" -> optional(text),
      "lastname" -> optional(text),

      "password" -> tuple(
        "main" -> text(minLength = 6),
        "confirm" -> text).verifying(
        "Passwords don't match",
        passwords => passwords._1 ==
          passwords._2)) {
      (username, email, firstname, lastname, passwords) => {
        user.username = username
        user.email = email
        user.firstname = firstname
        user.lastname = lastname
        user.password = passwords._1
        user
      }
    } {
      user => Some(user.username, user.email, user.firstname, user.lastname, ("", ""))
    }).fill(user)

  def index = AuthenticatedAdmin {
    implicit request => Ok(views.html.admin.users.index(User.findAll))
  }

  def add = AuthenticatedAdmin {
    implicit request => Ok(views.html.admin.users.add(userForm()))
  }

  def create = AuthenticatedAdmin {
    implicit request =>
      userForm().bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.admin.users.add(formWithErrors)),
        user => {
          user.save
          Redirect(routes.Users.index)
        })
  }

  def edit(id:Long) = AuthenticatedAdmin {
    implicit request =>
      User.findById(id).map { user =>
        Ok(views.html.admin.users.edit(user, userForm(user)))
      }.getOrElse(NotFound)
  }

  def update(id:Long) = AuthenticatedAdmin {
    implicit request =>
      User.findById(id).map { user =>
        userForm(user).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.admin.users.edit(user, formWithErrors)),
          user => {
            user.save
            Redirect(routes.Users.index)
          })
      }.getOrElse(NotFound)
  }
}
