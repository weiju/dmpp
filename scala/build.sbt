import Dependencies._

name := "dmpp"

organization := "org.dmpp"

version := "1.0"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature")

lazy val common = (project in file("dmpp-common")).settings(libraryDependencies ++= dmppDependencies)

lazy val cpu = (project in file("dmpp-cpu")).settings(libraryDependencies ++= dmppDependencies).dependsOn(common)

lazy val cymus = (project in file("dmpp-cymus")).dependsOn(common)

lazy val board = (project in file("dmpp-board")).settings(libraryDependencies ++= dmppDependencies).dependsOn(common, cymus)

lazy val debugger = (project in file("dmpp-debugger")).settings(libraryDependencies ++= dmppDependencies).dependsOn(common, cymus, board)

lazy val root = (project in file(".")).aggregate(debugger, board)
