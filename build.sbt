import Dependencies._

ThisBuild / scalaVersion     := "2.13.16"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.mkharytonau"
ThisBuild / organizationName := "mkharytonau"

lazy val root = (project in file("."))
  .settings(
    name := "BFT League Rating",
    libraryDependencies ++= Seq(
      "com.github.tototoshi" %% "scala-csv" % "2.0.0",
      "io.estatico" %% "newtype" % "0.4.4",
      munit % Test,
    ),
    scalacOptions += "-Ymacro-annotations"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
