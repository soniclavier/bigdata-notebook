package com.vishnuviswanath.spark.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

/**
  * Created by vviswanath on 2/18/18.
  *
  * bin/kafka-topics.sh --create --topic ratedata --zookeeper localhost:2181 --partitions 1 --replication-factor 1
  *
  * ?? The code doesn't write anything to console since the #of messages committed for an epoch does not match with writer parallelism.
  * Spark thinks that it has not read from all the parallel parts of the source. The problem might be with the rate source, which does not guarantee
  * the number of records produced per epoch.
  */
object HelloContinuousStreaming {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ContinuousStreaming example")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._
    val raw = spark
      .readStream
      .format("rate")
      .option("numPartitions", 5)
      .option("rowsPerSecond", 100)
      .load()

    val consoleQuery = raw
      .selectExpr("cast(value as STRING)")
      .writeStream
      .queryName("console-query")
      .format("console")
      .outputMode("update")
      //.outputMode("update")
      //.outputMode("complete") not supported since it requires an agg, and Continuous processing does not support aggregations.
      .trigger(Trigger.Continuous("1 second")) //how often to checkpoint
      .start()

    consoleQuery.awaitTermination()
  }

}
