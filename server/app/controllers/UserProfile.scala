package controllers

import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller

object UserProfile extends Controller with Secured {
  def form(user: User) = Form(
    mapping(
      "email" -> email,
      "firstname" -> optional(text),
      "lastname" -> optional(text)) {
      (email, firstname, lastname) => {
        user.email = email
        user.firstname = firstname
        user.lastname = lastname
        user
      }
    } {
      user => Some(user.email, user.firstname, user.lastname)
    }).fill(user)

  def changePasswordForm(user: User) = Form(
    mapping(
      "password" -> tuple(
        "main" -> text(minLength = 6),
        "confirm" -> text).verifying(
        "Passwords don't match", passwords => passwords._1 == passwords._2)) {
      (passwords) => {
        user.password = passwords._1
        user
      }
    } {
      user => Some(("", ""))
    }).fill(user)

  def index = Authenticated {
    implicit request =>
      User.findById(request.user.id).map {
        user =>
          Ok(views.html.userProfile.index(user))
      }.getOrElse(NotFound)
  }

  def edit = Authenticated {
    implicit request =>
      User.findById(request.user.id).map {
        user =>
          Ok(views.html.userProfile.edit(form(user)))
      }.getOrElse(NotFound)
  }

  def update = Authenticated {
    implicit request =>
      User.findById(request.user.id).map {
        user =>
          form(user).bindFromRequest.fold(
            formWithErrors => BadRequest(views.html.userProfile.edit(formWithErrors)),
            user => {
              user.save
              Redirect(routes.UserProfile.index)
            })
      }.getOrElse(NotFound)
  }

  def changePassword = Authenticated {
    implicit request =>
      User.findById(request.user.id).map {
        user =>
          Ok(views.html.userProfile.changePassword(changePasswordForm(user)))
      }.getOrElse(NotFound)
  }

  def updatePassword = Authenticated {
    implicit request =>
      User.findById(request.user.id).map {
        user =>
          changePasswordForm(user).bindFromRequest.fold(
            formWithErrors => BadRequest(views.html.userProfile.changePassword(formWithErrors)),
            user => {
              user.save
              Redirect(routes.UserProfile.index)
            })
      }.getOrElse(NotFound)
  }
}