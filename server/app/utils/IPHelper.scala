package utils

import java.net.InetAddress

object IPHelper {

  def nslookup(ip:String): String = {
    scala.util.control.Exception.allCatch[String].opt {
      InetAddress.getByName(ip).getHostName()
    }.getOrElse(ip)
  }
}
