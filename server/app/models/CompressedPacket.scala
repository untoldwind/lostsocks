package models

case class CompressedPacket(val data:Array[Byte], val endOfCommunication:Boolean) {

  def asString = new String(data, "UTF-8")
}

object CompressedPacket {
  def apply(strData:String, endOfCommunication:Boolean):CompressedPacket = CompressedPacket(strData.getBytes("UTF-8"), endOfCommunication)
}