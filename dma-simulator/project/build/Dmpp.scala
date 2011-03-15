import sbt._

class DmppProject(info: ProjectInfo) extends DefaultProject(info) {
  val mavenLocal = "Local Maven Reository" at "file://" +
    (Path.userHome / ".m2" / "repository").absolutePath
}

