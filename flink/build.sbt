val flink = "org.apache.flink" % "flink-core" % "0.10.2"
val flinkclients = "org.apache.flink" % "flink-clients" % "0.10.2"
val flinkstreamingcore = "org.apache.flink" % "flink-streaming-core" % "0.9.1"
val flinkstreamingscala = "org.apache.flink" % "flink-streaming-scala" % "0.10.2"

val main = "com.vishnu.flink.streaming.FlinkStreamingWordCount"

name := "flink-vishnu"
mainClass in (Compile, run) := Some(main)
mainClass in (Compile, packageBin) := Some(main)

lazy val commonSettings = Seq(
  organization := "com.vishnu",
  version := "1.0",
  scalaVersion := "2.10.5"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    name := "flink-vishnu",
    libraryDependencies += flink,
    libraryDependencies += flinkclients,
    libraryDependencies += flinkstreamingscala,
    libraryDependencies += flinkstreamingcore,
    retrieveManaged := true
  )

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case x if x.contains("unused") => MergeStrategy.last
    case "application.conf" => MergeStrategy.concat
    case "unwanted.txt"     => MergeStrategy.discard
    case x => old(x)
  }
}

