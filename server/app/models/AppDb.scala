package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object AppDb extends Schema {
  val users = table[User]

  val roles = table[Role]

  val userRoles =
    manyToManyRelation(users, roles, "User_Role").
      via[UserRole]((u, r, ur) => (u.id === ur.userId, r.id === ur.roleId))
}