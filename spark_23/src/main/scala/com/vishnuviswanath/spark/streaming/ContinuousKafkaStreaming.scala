package com.vishnuviswanath.spark.streaming

import java.sql.Timestamp

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger


//convert aggregates into typed data
case class CarEvent(carId: String, speed: Option[Int], acceleration: Option[Double], timestamp: Timestamp)
object CarEvent {
  def apply(rawStr: String): CarEvent = {
    val parts = rawStr.split(",")
    CarEvent(parts(0), Some(Integer.parseInt(parts(1))), Some(java.lang.Double.parseDouble(parts(2))), new Timestamp(parts(3).toLong))
  }
}

/**
  * Created by vviswanath on 2/18/18.
  */
object ContinuousKafkaStreaming {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ContinuousStreaming Kafka example")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val raw = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "cars")
      .load()

    //supported operations in Continuous Processing includes - Map, Filter, Project
    val fastCars = raw
      .selectExpr("CAST(value as STRING)") //project
      .map(r ⇒ CarEvent(r.getString(0))) //map
      .filter("speed > 70") //filter
      //.filter(c ⇒ c.speed.getOrElse(0) > 70) //TypedFilter not supported in continuous processing,


   val consoleQuery = fastCars
      .writeStream
      .format("console")
      .outputMode("append")
      //.outputMode("update")
      //.outputMode("complete") not supported since it requires an agg, and Continuous processing does not support aggregations.
      .trigger(Trigger.Continuous("1 second"))
      .start()


    val kafkaSinkQuery = fastCars
      .selectExpr("CAST(carId as STRING) as value") //kafka needs a value field
      .writeStream
      .format("kafka")
      .outputMode("update")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", "fastcars")
      .option("checkpointLocation", "/tmp/spark/continuousCheckpoint")
      .outputMode("update")
      .trigger(Trigger.Continuous("10 seconds")) //how often to checkpoint the offsets,
      .start()

    spark.streams.awaitAnyTermination()

  }

}
