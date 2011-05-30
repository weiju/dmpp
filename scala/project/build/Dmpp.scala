import sbt._

class DmppProject(info: ProjectInfo) extends ParentProject(info) {
  val mavenLocal = "Local Maven Repository" at "file://" +
    (Path.userHome / ".m2" / "repository").absolutePath
  val mahatma68k = "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT" % "compile->default"
  val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"

  lazy val common   = project("dmpp-common", "dmpp-common")
  lazy val cpu      = project("dmpp-cpu", "dmpp-cpu", common)
  lazy val cymus    = project("dmpp-cymus", "dmpp-cymus", common)
  lazy val board    = project("dmpp-board", "dmpp-board",
                              cymus, cpu, common)
  lazy val debugger = project("dmpp-debugger", "dmpp-debugger",
                              cymus, board, common)
}

