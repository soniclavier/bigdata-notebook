package com.vishnuviswanath.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * Created by vviswanath on 1/10/18.
  *
  * Explore Spark streaming aggregations.
  */
object StreamingAggregations {

  //convert aggregates into typed data
  case class CarEvent(car: String, speed: Option[Int], acceleration: Option[Double])

  def main(args: Array[String]): Unit = {

    //create a spark session, and run it on local mode
    val spark = SparkSession.builder()
      .appName("StreaminAggregations")
      .master("local[*]")
      .getOrCreate()

    //spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    //define the schema
    val schema = StructType(
      StructField("car", StringType) ::
      StructField("speed", IntegerType) ::
      StructField("acceleration", DoubleType) :: Nil)

    //read the source
    val cars: DataFrame = spark
      .readStream
      .schema(schema)
      .csv("/Users/vviswanath/Downloads/streaming_input_dir/cars/")

    //do aggregates
    val aggregates = cars
      .groupBy("car")
      .agg(
        "speed" → "max",
        "acceleration" → "avg")
      .withColumnRenamed("max(speed)", "speed")
      .withColumnRenamed("avg(acceleration)", "acceleration")

    aggregates.printSchema()
    aggregates.explain()

    val typedAggregates = aggregates.as[CarEvent]
    val filtered  = typedAggregates
      .filter(_.speed.exists(_ > 70))
      .where("acceleration > 10")
      .repartition(10)

    val query = filtered
      .writeStream
      .queryName("fastVehicles")
      .partitionBy("car")
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()

  }
}
