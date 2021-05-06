name := """first-catering-ltd-restful-api"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.5"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % Test
libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % Test