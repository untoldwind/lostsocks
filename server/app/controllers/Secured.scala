package controllers

import models.User
import models.UserInfo
import play.api.Play.current
import play.api.mvc._
import play.api.http.HeaderNames
import org.apache.commons.codec.binary.Base64
import play.api.cache.Cache
import utils.crypto.PasswordEncoder

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

  def AuthenticatedAdmin[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) =
    Action(p) {
      implicit request =>
        getUserInfo.map {
          user => if(user.canAdmin) f(AuthenticatedRequest(user, request)) else Unauthorized("Unauthorized")
        }.getOrElse(onUnauthorized)
    }

  def AuthenticatedAdmin[A](f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] =
    AuthenticatedAdmin(parse.anyContent)(f)

  val BasicAuthenticationPattern = """Basic[ ]+([0-9a-zA-Z+\/=]+)""".r

  def BasicAuthenticated[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) =
    Action(p) {
      implicit request =>
        request.headers.get(HeaderNames.AUTHORIZATION).flatMap { authorization =>
          authorization match {
            case BasicAuthenticationPattern(base64) =>
              val credentials = new String(Base64.decodeBase64(base64.getBytes("UTF-8")), "UTF-8")
              credentials.split(":") match {
                case Array(username, password) =>
                  val userOpt:Option[(UserInfo, String)] = Cache.getOrElse("basic-" + username) {
                    println(">>>> Getty: " + username)
                    User.findByUsername(username).map { user => (UserInfo(user.id, user.username, user.roleNames), user.hashedPassword) }
                  }
                  userOpt.flatMap { case (userInfo, hashedPassword) =>
                    if (PasswordEncoder.verify(password, hashedPassword)) {
                      Some(f(AuthenticatedRequest(userInfo, request)))
                    } else {
                      None
                    }
                  }
                case _ => None
              }
            case _ => None
          }
        }.getOrElse(Unauthorized("Authorization Required").withHeaders(WWW_AUTHENTICATE -> """Basic realm="Lostsocks""""))
    }

  def BasicAuthenticated[A](f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] =
    BasicAuthenticated(parse.anyContent)(f)
}
