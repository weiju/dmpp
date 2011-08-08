name         := "dmpp"

version      := "1.0"

organization := "org.dmpp"

scalaVersion := "2.9.0-1"

libraryDependencies += "org.mahatma68k" % "mahatma68k" % "1.0-SNAPSHOT"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
