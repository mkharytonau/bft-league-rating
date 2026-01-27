import Dependencies._

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.mkharytonau"
ThisBuild / organizationName := "mkharytonau"

lazy val root = (project in file("."))
  .settings(
    name := "BFT League Rating",
    evictionErrorLevel := Level.Info,
    libraryDependencies ++= Seq(
      "com.github.tototoshi" %% "scala-csv" % "2.0.0",
      "io.estatico" %% "newtype" % "0.4.4",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
      "io.circe" %% "circe-core" % "0.14.14",
      "io.circe" %% "circe-generic" % "0.14.14",
      "io.circe" %% "circe-parser" % "0.14.14",
      "tf.tofu" %% "derevo-circe" % "0.14.0",
      "com.ibm.icu" % "icu4j" % "78.2",
      "org.apache.commons" % "commons-text" % "1.15.0",
      munit % Test
    ),
    scalacOptions += "-Ymacro-annotations"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
