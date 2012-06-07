package utils.crypto

import java.security.SecureRandom
import java.io.FileInputStream
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import org.apache.commons.codec.binary.Base64
import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer

object PasswordEncoder {
  def encrypt(password: String, algorithm: String = "SSHA256", salt: Option[Array[Byte]] = Some(randomSalt()),
    iterations: Short = 100): String = {

    val passwordData = password.getBytes("UTF-8")
    val (prefix, messageDigest) = algorithm.toUpperCase match {
      case "SHA256" | "SSHA256" => (if (salt.isDefined) "{SSHA256}" else "{SHA256}", MessageDigest
        .getInstance("SHA-256"))
      case "SHA" | "SSHA" => (if (salt.isDefined) "{SSHA}" else "{SHA}", MessageDigest.getInstance("SHA-1"))
      case "MD5" | "SMD5" => (if (salt.isDefined) "{SMD5}" else "{MD5}", MessageDigest.getInstance("MD5"))
      case _ => throw new UnsupportedOperationException("Not implemented")
    }

    val digest = new Array[Byte](messageDigest.getDigestLength());

    for (i <- 0 to iterations - 1) {
      messageDigest.reset()
      if (i > 0) messageDigest.update(digest)
      messageDigest.update(passwordData)

      salt.map {
        messageDigest.update(_)
      }

      messageDigest.digest.copyToArray(digest)
    }

    val out = Array((iterations >>> 8 & 0xff).toByte, (iterations & 0xff).toByte) ++ digest ++
      salt.getOrElse(Array.empty)

    prefix + new String(Base64.encodeBase64(out, false), "UTF-8")
  }

  def verify(password: String, encryptedPassword: String): Boolean = {

    val passwordData = password.getBytes("UTF-8")
    val Algorithm = """\{([^\}]*)\}(.*)""".r
    val (messageDigest, base64) = encryptedPassword match {
      case Algorithm("SHA256" | "SSHA256", base64) => (MessageDigest.getInstance("SHA-256"), base64)
      case Algorithm("SHA" | "SSHA", base64) => (MessageDigest.getInstance("SHA-1"), base64)
      case Algorithm("MD5" | "SMD5", base64) => (MessageDigest.getInstance("MD5"), base64)
      case _ => return false
    }
    val size = messageDigest.getDigestLength()
    val data = Base64.decodeBase64(base64.getBytes("UTF-8"))
    val iterations = ((data(0) & 0xff) << 8 | data(1) & 0xff).toShort
    val orig = data.slice(2, size + 2)
    val salt = if (data.length > size + 2) Some(data.slice(size + 2, data.length)) else None

    val digest = new Array[Byte](size);

    for (i <- 0 to iterations - 1) {
      messageDigest.reset();
      if (i > 0) messageDigest.update(digest)
      messageDigest.update(passwordData);

      salt.map {
        messageDigest.update(_)
      }

      messageDigest.digest.copyToArray(digest)
    }

    MessageDigest.isEqual(digest, orig);
  }

  def randomSalt(length: Short = 64): Array[Byte] = {

    val salt = new Array[Byte](length)

    random.nextBytes(salt)

    salt
  }

  private val random: SecureRandom = {
    try {
      val instance = SecureRandom.getInstance("SHA1PRNG")
      val urandom = new File("/dev/urandom")

      if (urandom.exists()) {
        val is: FileInputStream = new FileInputStream(urandom);
        val salt = Iterator.continually(is.read).take(8192).map(_.toByte).toArray
        is.close()
        instance.setSeed(salt)
      }
      instance
    } catch {
      case _ => new SecureRandom()
    }
  }
}