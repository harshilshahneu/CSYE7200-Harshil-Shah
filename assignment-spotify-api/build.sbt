ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "assignment-spotify-api"
  )

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.8.0",
  "io.spray" %%  "spray-json" % "1.3.6"
)
