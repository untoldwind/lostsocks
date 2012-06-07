package models

import org.squeryl.KeyedEntity
import org.squeryl.dsl.OneToMany
import org.squeryl.PrimitiveTypeMode._
import utils.crypto.PasswordEncoder
import org.joda.time.DateTimeZone
import org.squeryl.annotations.Transient
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.Format
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString

class User(
  val id: Long,
  var username: String,
  var email: String,
  var firstname: Option[String],
  var lastname: Option[String],
  var hashedPassword: String) extends KeyedEntity[Long] {

  def this() = this(0, "", "", None, None, "")

  lazy val roles = AppDb.userRoles.left(this)

  def displayString = lastname.map(l => firstname.map(f => f + " ").getOrElse("") + l).getOrElse(username)

  @Transient
  def password = ""

  def password_=(newPassword: String) = {

    hashedPassword = PasswordEncoder.encrypt(newPassword)
  }

  def roleNames = inTransaction {
    this.roles.map(r => r.name).toSeq
  }

  def save = inTransaction {
    AppDb.users.insertOrUpdate(this)
  }

  def delete = inTransaction {
    if (isPersisted)
      AppDb.users.delete(id)
  }
}

object User {
  def apply(username: String,
    email: String,
    firstname: Option[String],
    lastname: Option[String],
    password: String) = new
      User(0, username, email, firstname, lastname, PasswordEncoder.encrypt(password))

  def findAll: Seq[User] = inTransaction {
    from(AppDb.users)(u => select(u) orderBy(u.lastname.asc, u.username.asc)).toList
  }

  def findById(userId: Long): Option[User] = inTransaction {
    AppDb.users.lookup(userId)
  }

  def authenticate(username: String, password: String): Option[User] = inTransaction {
    val user = AppDb.users.where(u => u.username === username).headOption

    user.filter {
      user => PasswordEncoder.verify(password, user.hashedPassword)
    }
  }

  implicit object UserFormat extends Format[User] {
    override def reads(json: JsValue): User = {

      val result = new User()
      result
    }

    override def writes(user: User): JsValue = JsObject(List(
                                                              "id" -> JsNumber(user.id),
                                                              "username" -> JsString(user.username),
                                                              "email" -> JsString(user.email),
                                                              "display" -> JsString(user.displayString))
      ::: user.firstname.map(f => List("firstname" -> JsString(f))).getOrElse(List.empty)
      ::: user.lastname.map(l => List("lastname" -> JsString(l))).getOrElse(List.empty)
                                                       )
  }

}