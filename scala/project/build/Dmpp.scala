import sbt._

class DmppProject(info: ProjectInfo) extends ParentProject(info) {
  val mavenLocal = "Local Maven Reository" at "file://" +
    (Path.userHome / ".m2" / "repository").absolutePath
  val mahatma68k = "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT" % "compile->default"
  val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"

  lazy val cpu   = project("dmpp-cpu", "dmpp-cpu")
  lazy val cymus = project("dmpp-cymus", "dmpp-cymus", cpu)
  lazy val board = project("dmpp-board", "dmpp-board", cymus, cpu)
  lazy val debugger = project("dmpp-debugger", "dmpp-debugger", cymus, board)
}
