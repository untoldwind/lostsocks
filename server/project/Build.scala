import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "com.objectcode.lostsocks"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
      "com.objectcode.lostsocks" % "common" % "0.1-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += (
        "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
      )
    )

}
