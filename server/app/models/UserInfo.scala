package models

object SpyMode extends Enumeration {
  type Type = Value
  val NONE, CLIENT, SERVER, BOTH = Value
}

class UserInfo {
  var login:String = null
  var password:String = null
  var authorizedTime:Long = 0
  var spyMode:SpyMode.Type = SpyMode.NONE
}
