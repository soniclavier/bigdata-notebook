name := "spark_23"
version := "1.0"
scalaVersion := "2.11.9"

val sparkVersion = "2.4.0-SNAPSHOT"
val kafkaVersion = "0.10.2.1"

resolvers += Resolver.mavenLocal

libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "org.apache.kafka" % "kafka-clients" % kafkaVersion

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % Test

    