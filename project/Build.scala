import javax.sound.midi.Sequence
import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appVersion = "0.1-SNAPSHOT"

  lazy val parent = Project(id = "parent",
    base = file(".")).settings(
    organization := "com.objectcode.lostsocks"
  ) aggregate(client, server)

  lazy val client = Project(id = "client", base = file("client")).settings(
    organization := "com.objectcode.lostsocks",
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.2"
    ),
    unmanagedJars in Compile += file(System.getProperty("java.home") + "/lib/javaws.jar")
  )

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "org.squeryl" %% "squeryl" % "0.9.5-RC1"
  )

  lazy val server = PlayProject("server", appVersion, appDependencies, mainLang = SCALA,
    path = file("server")).settings(
    organization := "com.objectcode.lostsocks"
  )
}
