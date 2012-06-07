import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "server"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
      "com.objectcode.lostsocks" % "common_2.9.1" % "0.1-SNAPSHOT",
      "postgresql" % "postgresql" % "9.0-801.jdbc4",
      "org.squeryl" %% "squeryl" % "0.9.5-RC1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      organization := "com.objectcode.lostsocks"
    )

}
