import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appVersion      = "0.1-SNAPSHOT"

  lazy val parent = Project(id = "parent",
    base = file(".")).settings(
    organization := "com.objectcode.lostsocks"
  ) aggregate(common, server)

  lazy val common = Project(id = "common", base = file("common")).settings(
    organization := "com.objectcode.lostsocks"
  )

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "org.squeryl" %% "squeryl" % "0.9.5-RC1"
  )

  lazy val server = PlayProject("server", appVersion, appDependencies, mainLang = SCALA,
    path = file("server")).settings(
      organization := "com.objectcode.lostsocks"
    ) dependsOn(common)
}
