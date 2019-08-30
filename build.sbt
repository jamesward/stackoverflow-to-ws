enablePlugins(PlayScala)

name := "stackoverflow-to-ws"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  guice,
  "com.google.cloud" % "google-cloud-bigquery" % "1.87.0",
)

sources in (Compile,doc) := Seq.empty
