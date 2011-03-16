import sbt._

class DmppProject(info: ProjectInfo) extends DefaultProject(info) {
  val mavenLocal = "Local Maven Reository" at "file://" +
    (Path.userHome / ".m2" / "repository").absolutePath

  val dmpp_board = "org.dmpp" % "dmpp-board" % "1.0-SNAPSHOT"
}

