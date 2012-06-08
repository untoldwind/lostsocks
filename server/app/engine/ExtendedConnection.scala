package engine

import play.api.libs.openid.UserInfo
import java.util.Date
import akka.actor.ActorRef
import play.api.libs.iteratee.Input
import java.util.concurrent.LinkedBlockingQueue
import akka.util.ByteString

class ExtendedConnection {
  var ip:String = "?"
  var iprev:String = "?"
  var user:UserInfo = null
  val creationDate = new Date()
  var lastAccessDate = new Date()
  var uploadedBytes:Long = 0
  var downloadedBytes:Long = 0
  var destIP:String = "?"
  var destIPrev:String = "?"
  var destPort:Int = 0
  var currentUploadSpeed:Double = 0
  var currentDownloadSpeed:Double = 0
  var timeout:Long = 0
  var authorizedTime:Long = 0

  var connectionActor:ActorRef = null
  var downQueue:LinkedBlockingQueue[Input[ByteString]] = null
}
