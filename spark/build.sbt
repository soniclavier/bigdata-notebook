val spark_mllib = "org.apache.spark" % "spark-mllib_2.11" % "1.5.2"
val spark_core = "org.apache.spark" % "spark-core_2.11" % "1.5.2"
val spark_csv = "com.databricks" % "spark-csv_2.11" % "1.2.0"

lazy val commonSettings = Seq(
  organization := "com.vishnu",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(
    name := "spark_ml_exmple",
    libraryDependencies += spark_mllib,
    libraryDependencies += spark_core,
    libraryDependencies += spark_csv,
    retrieveManaged := true
  )

