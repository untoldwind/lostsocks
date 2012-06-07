package models

import org.squeryl.PrimitiveTypeMode.compositeKey
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity

class UserRole(
  val userId: Long,
  val roleId: Long) extends KeyedEntity[CompositeKey2[Long, Long]] {

  def id = compositeKey(userId, roleId)
}