package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

class Role(
            val id: Long,
            val name: String) extends KeyedEntity[Long] {

  def this() = this(0, "")

  lazy val users = AppDb.userRoles.right(this)
}

object Role {
  def findById(roleId: Long): Option[Role] = inTransaction {
    AppDb.roles.lookup(roleId)
  }

}