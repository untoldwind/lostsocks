package engine

import play.api.libs.openid.UserInfo
import java.util.Date
import akka.actor.ActorRef
import play.api.libs.iteratee.Input
import java.util.concurrent.LinkedBlockingQueue
import akka.util.ByteString

class ExtendedConnection(val connectionId: String, val connectionActor:ActorRef, val downQueue:DownStreamQueue) {
  var ip: String = "?"
  var iprev: String = "?"
  var user: UserInfo = null
  val creationDate = new Date()
  private var _lastAccessDate = new Date()
  private var _lastLastAccessDate = new Date()
  var uploadedBytes: Long = 0
  var downloadedBytes: Long = 0
  var destIP: String = "?"
  var destIPrev: String = "?"
  var destPort: Int = 0
  var currentUploadSpeed: Double = 0
  var currentDownloadSpeed: Double = 0
  var timeout: Long = 0
  var authorizedTime: Long = 0

  def lastAccessDate = _lastAccessDate

  def lastAccessDate_=(date: Date) = {
    _lastLastAccessDate = _lastAccessDate
    _lastAccessDate = date
  }

  def incrementUp(dataSize: Long) = {
    val div = 1 + _lastAccessDate.getTime - _lastLastAccessDate.getTime
    currentUploadSpeed = dataSize.toDouble / div
  }

  def incrementDown(dataSize: Long) = {
    // Add the received bytes
    downloadedBytes += dataSize

    // Update the download speed
    val div = 1 + _lastAccessDate.getTime - _lastLastAccessDate.getTime
    currentDownloadSpeed = dataSize.toDouble / div

  }
}
