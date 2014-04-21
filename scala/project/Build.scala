import sbt._
import Keys._

object MyBuild extends Build {

  override lazy val settings = super.settings ++ buildSettings

  def buildSettings = Seq(
    organization := "org.dmpp",
    version := "1.0",
    scalaVersion := "2.11.0",
    javacOptions in Compile ++= Seq("-target", "6", "-source", "6"),
    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

  lazy val root = Project("root", file(".")) aggregate(debugger)
  lazy val common = Project("dmpp-common", file("dmpp-common")) settings(buildDependencies : _*)
  lazy val debugger = Project("dmpp-debugger", file("dmpp-debugger")) dependsOn(common, cpu, cymus, board)
  lazy val board = Project("dmpp-board", file("dmpp-board")) settings(buildDependencies : _*) dependsOn(common, cpu, cymus)
  lazy val cymus = Project("dmpp-cymus", file("dmpp-cymus")) dependsOn(common)
  lazy val cpu = Project("dmpp-cpu", file("dmpp-cpu")) dependsOn(common)

  def testDependencies = libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.1.3",
    "junit" % "junit" % "4.10")

  def buildDependencies = libraryDependencies ++= Seq(
    "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT",
    "org.scalatest" % "scalatest_2.11" % "2.1.3",
    "junit" % "junit" % "4.10",
    "org.scala-lang" % "scala-swing" % "2.11.0-M7"
  )
}

