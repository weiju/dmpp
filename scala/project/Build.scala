import sbt._
import Keys._

object MyBuild extends Build {
  val ScalaVersion = "2.9.0-1"
  lazy val root = Project("root", file(".")) aggregate(debugger)
  lazy val common = Project("dmpp-common", file("dmpp-common")) settings(resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository", libraryDependencies += "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT", scalaVersion := ScalaVersion)
  lazy val debugger = Project("dmpp-debugger",
    file("dmpp-debugger")) settings(resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                                    libraryDependencies += "org.scala-lang" % "scala-swing" % ScalaVersion,
                                    libraryDependencies += "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT",
                                    scalaVersion := ScalaVersion) dependsOn(common, cpu, cymus, board)
  lazy val board = Project("dmpp-board",
    file("dmpp-board")) settings(resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                                 libraryDependencies += "org.scala-lang" % "scala-swing" % ScalaVersion,
                                 libraryDependencies += "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT",
                                 scalaVersion := ScalaVersion) dependsOn(common, cpu, cymus)
  lazy val cymus = Project("dmpp-cymus", file("dmpp-cymus")) settings(resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository", libraryDependencies += "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT", scalaVersion := ScalaVersion) dependsOn(common)
  lazy val cpu = Project("dmpp-cpu", file("dmpp-cpu")) settings(resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository", libraryDependencies += "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT", scalaVersion := ScalaVersion) dependsOn(common)
}

