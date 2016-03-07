val spark_mllib = "org.apache.spark" % "spark-mllib_2.10" % "1.5.2" % "provided"
val spark_core = "org.apache.spark" % "spark-core_2.10" % "1.5.2 "% "provided"
val spark_csv = "com.databricks" % "spark-csv_2.10" % "1.2.0" % "provided"
val spark_streaming = "org.apache.spark" % "spark-streaming_2.10" % "1.6.0" % "provided"  
val kafka_core = "org.apache.kafka" % "kafka_2.10" % "0.8.2.1"
val flume_streaming = "org.apache.spark" % "spark-streaming-flume_2.10" % "1.6.0"  exclude("org.spark-project.spark", 
"unused")
val kafka_streaming = "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.0"


name := "spark-vishnu"

lazy val commonSettings = Seq(
  organization := "com.vishnu",
  version := "1.0",
  scalaVersion := "2.10.5"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    name := "spark_examples",
    libraryDependencies += spark_mllib,
    libraryDependencies += spark_core,
    libraryDependencies += spark_csv,
    libraryDependencies += spark_streaming,
    libraryDependencies += flume_streaming,
    libraryDependencies += kafka_streaming,
    libraryDependencies += kafka_core,
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

