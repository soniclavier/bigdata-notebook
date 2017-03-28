import sbt.Keys.name

//resolvers in ThisBuild ++= Seq("Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/", Resolver.mavenLocal)
resolvers in ThisBuild ++= Seq(Resolver.mavenLocal)

scalaVersion in ThisBuild := "2.11.3"

val flinkVersion = "1.3.1-SNAPSHOT"

val flink_scala = "org.apache.flink" %% "flink-scala" % flinkVersion % "provided"
val flink_clients = "org.apache.flink" %% "flink-clients" % flinkVersion % "provided"
val flink_streaming = "org.apache.flink" %% "flink-streaming-scala" %flinkVersion % "provided"
val flink_rocks_db = "org.apache.flink" %% "flink-statebackend-rocksdb" % flinkVersion % "provided"
val joda_time = "joda-time" % "joda-time" % "2.9.4"

val main = "com.vishnu.flink.streaming.queryablestate.QuerybleStateStream"

mainClass in (Compile, run) := Some(main)
mainClass in (Compile, packageBin) := Some(main)

lazy val commonSettings = Seq(
  organization := "com.vishnuviswanath",
  version := "1.0",
  name := "flink-vishnu"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    name := "flink-vishnu",
    libraryDependencies += flink_scala,
    libraryDependencies += flink_clients,
    libraryDependencies += flink_streaming,
    libraryDependencies += flink_rocks_db,
    libraryDependencies += joda_time,
    retrieveManaged := true
  )

lazy val mainRunner = project.in(file("mainRunner")).dependsOn(RootProject(file("."))).settings(
  // we set all provided dependencies to none, so that they are included in the classpath of mainRunner
  libraryDependencies := (libraryDependencies in RootProject(file("."))).value.map{
    module =>
      if (module.configurations.equals(Some("provided"))) {
        module.copy(configurations = None)
      } else {
        module
      }
  }
)


