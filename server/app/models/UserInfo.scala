package models

case class UserInfo(val id: Long,
                    val username: String,
                    val roles: Seq[String]) {

  def canAdmin = {
    roles.contains("Administrators")
  }
}