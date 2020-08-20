enablePlugins(PlayScala)

name := "stackoverflow-to-ws"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  guice,
  "com.google.cloud" % "google-cloud-bigquery" % "1.116.0",
)

sources in (Compile,doc) := Seq.empty

dockerBaseImage := "adoptopenjdk/openjdk8"
