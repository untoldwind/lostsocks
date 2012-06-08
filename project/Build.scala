import sbt._
import Keys._
import PlayProject._
import sbtassembly.Plugin._
import AssemblyKeys._

object ApplicationBuild extends Build {

  val appVersion = "0.1-SNAPSHOT"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1-SNAPSHOT",
    organization := "com.objectcode.lostsocks"
  )

  lazy val parent = Project(id = "parent",
    base = file("."), settings = buildSettings).settings(
    crossPaths := false
  ) aggregate(client, server)

  lazy val client = Project(id = "client", base = file("client"),
    settings = buildSettings ++ assemblySettings ++ addArtifact(Artifact("client", "assembly"), assembly) ).settings(
    organization := "com.objectcode.lostsocks",
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.2"
    ),
    crossPaths := false,
    unmanagedJars in Compile += file(System.getProperty("java.home") + "/lib/javaws.jar"),
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      cp filter { p => p.data.getName == "javaws.jar" || p.data.getName == "scala-library.jar" }
    },
    mainClass in assembly := Some("com.objectcode.lostsocks.client.Main")
  )

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "org.squeryl" %% "squeryl" % "0.9.5-RC1"
  )

  lazy val server = PlayProject("server", appVersion, appDependencies, mainLang = SCALA,
    path = file("server"), settings = buildSettings)
}
