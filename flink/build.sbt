val flink = "org.apache.flink" % "flink-core" % "1.0.0"
val flinkclients = "org.apache.flink" % "flink-clients_2.11" % "1.0.0"
val flinkstreaming = "org.apache.flink" % "flink-streaming-scala_2.11" % "1.0.0"

val main = "com.vishnu.flink.streaming.FlinkStreamingWordCount"

name := "flink-vishnu"
mainClass in (Compile, run) := Some(main)
mainClass in (Compile, packageBin) := Some(main)

lazy val commonSettings = Seq(
  organization := "com.vishnu",
  version := "1.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    name := "flink-vishnu",
    libraryDependencies += flink,
    libraryDependencies += flinkclients,
    libraryDependencies += flinkstreaming,
    retrieveManaged := true
  )



