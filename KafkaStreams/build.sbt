name := "KafkaStreams"

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.vishnuviswanath"

val kafkaStreamsVersion = "0.10.2.0"

val kafkaDependencies = Seq(
  "org.apache.kafka" % "kafka-streams" % kafkaStreamsVersion)

val otherDependencies = Seq(
    "com.esotericsoftware.kryo" % "kryo" % "2.24.0"
)

val main = "com.vishnuviswanath.kafka.streams.KafkaStreamsExample"
mainClass in (Compile, run) := Some(main)
mainClass in (Compile, packageBin) := Some(main)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= kafkaDependencies,
    libraryDependencies ++= otherDependencies
  )


    