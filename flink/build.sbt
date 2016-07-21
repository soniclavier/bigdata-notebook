val flink_scala = "org.apache.flink" %% "flink-scala" % "1.0.0"
val flink_clients = "org.apache.flink" %% "flink-clients" % "1.0.0"
val flink_streaming = "org.apache.flink" %% "flink-streaming-scala" % "1.0.0"
val joda_time = "joda-time" % "joda-time" % "2.9.4"

val main = "com.vishnu.flink.streaming.FlinkStreamingTest"
//val main = "com.vishnu.flink.dataset.WordCount"


name := "flink-vishnu"
mainClass in (Compile, run) := Some(main)
mainClass in (Compile, packageBin) := Some(main)

lazy val commonSettings = Seq(
  organization := "com.vishnu",
  version := "1.0",
  scalaVersion := "2.10.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    name := "flink-vishnu",
    libraryDependencies += flink_scala,
    libraryDependencies += flink_clients,
    libraryDependencies += flink_streaming,
    libraryDependencies += joda_time,
    retrieveManaged := true
  )


