import sbt._

class DmppProject(info: ProjectInfo) extends DefaultProject(info) {
  val mavenLocal = "Local Maven Reository" at "file://" +
    (Path.userHome / ".m2" / "repository").absolutePath
  val mahatma68k = "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT" % "compile->default"
  val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test"

  lazy val cymus = project("dmpp-cymus", "dmpp-cymus")
  lazy val board = project("dmpp-board", "dmpp-board", cymus)
}
