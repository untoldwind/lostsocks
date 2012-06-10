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
    settings = buildSettings ++ assemblySettings ++ addArtifact(Artifact("client", "assembly"), assembly)).settings(
    organization := "com.objectcode.lostsocks",
    libraryDependencies ++= Seq(
      "io.netty" % "netty" % "3.3.0.Final",
      ("com.ning" % "async-http-client" % "1.7.0" notTransitive())
        .exclude("org.jboss.netty", "netty"),
      "org.slf4j" % "slf4j-api" % "1.6.4",
      "ch.qos.logback" % "logback-core" % "1.0.0",
      "ch.qos.logback" % "logback-classic" % "1.0.0"
    ),
    crossPaths := false,
    excludedJars in assembly <<= (fullClasspath in assembly) map {
      cp =>
        cp filter {
          p => p.data.getName == "scala-library.jar"
        }
    },
    mainClass in assembly := Some("com.objectcode.lostsocks.client.Main"),
    playStage <<= (baseDirectory, packagedArtifacts, streams) map { (root, artifacts, s) =>
      artifacts.foreach {
        case (Artifact("client", _ , _, Some("assembly"), _, _, _), _file) =>
          val destPath =  root / ".." / "server" / "public" / "client-executable.jar"
          s.log.info("Copy " + _file + " to " + destPath)
          IO.copyFile(_file, destPath, true)
        case _ =>
      }
    }
  )

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "9.0-801.jdbc4",
    "org.squeryl" %% "squeryl" % "0.9.5-RC1"
  )

  lazy val server = PlayProject("server", appVersion, appDependencies, mainLang = SCALA,
    path = file("server"), settings = buildSettings).settings(
  ).dependsOn(client)
}
