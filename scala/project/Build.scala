import sbt._

object Dependencies {
  val junit =  "junit" % "junit" % "4.12" % "test"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.11.0-M7"

  val dmppDependencies = Seq(junit, scalaTest, scalaSwing)
}
