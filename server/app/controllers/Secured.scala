package controllers

import models.UserInfo
import play.api.mvc._

case class AuthenticatedRequest[A](val user: UserInfo,
                                   val request: Request[A]) extends WrappedRequest(request) {

  def ajax = request.queryString.get("ajax").flatMap(_.headOption).map(_.toBoolean).getOrElse(false)
}

trait Secured {
  self: Controller =>

  protected def getUserInfo(implicit request: RequestHeader) =
    request.session.get("userId").flatMap {
      userId => request.session.get("username").flatMap {
        username => request.session.get("roles").map {
          roles => UserInfo(userId.toLong, username, roles.split(";"))
        }
      }
    }

  private def onUnauthorized(implicit request: RequestHeader) = Results.Redirect(routes.Login.index)

  def Authenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) =
    Action(p) {
      implicit request =>
        getUserInfo.map {
          user => f(AuthenticatedRequest(user, request))
        }.getOrElse(onUnauthorized)
    }

  def Authenticated(f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] =
    Authenticated(parse.anyContent)(f)

}
