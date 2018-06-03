ThisBuild / resolvers ++= Seq("Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/", Resolver.mavenLocal)

ThisBuild / scalaVersion := "2.11.7"

val flinkVersion = "1.5.0"
val kafkaVersion = "0.11.0.2"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" %flinkVersion % "provided",
  "org.apache.flink" %% "flink-statebackend-rocksdb" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-queryable-state-client-java" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-queryable-state-runtime" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-cep-scala" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka-0.11" % flinkVersion
)

val otherDependencies = Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "joda-time" % "joda-time" % "2.9.4",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",
  "log4j" % "log4j" % "1.2.17"
)

val main = "com.vishnu.flink.streaming.queryablestate.QuerybleStateStream"

Compile / run / mainClass := Some(main)

assembly / mainClass := Some(main)

Compile / run := Defaults.runTask(Compile / fullClasspath,
                                  Compile / run / mainClass,
                                  Compile / run / runner).evaluated

lazy val commonSettings = Seq(
  organization := "com.vishnuviswanath",
  version := "1.0",
  name := "flink-examples"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    libraryDependencies ++= flinkDependencies,
    libraryDependencies ++= otherDependencies,
    retrieveManaged := true
  )


lazy val mainRunner = project.in(file("mainRunner")).dependsOn(RootProject(file("."))).settings(
  // we set all provided dependencies to none, so that they are included in the classpath of mainRunner
  libraryDependencies := (libraryDependencies in RootProject(file("."))).value.map{
    module => module.configurations match {
      case Some("provided") => module.withConfigurations(None)
      case _ => module
    }
  }
)


