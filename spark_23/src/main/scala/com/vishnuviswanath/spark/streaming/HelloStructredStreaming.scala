package com.vishnuviswanath.spark.streaming

import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by vviswanath on 1/9/18.
  *
  * Word count program to get started with Spark Structured Streaming
  */
object HelloStructredStreaming {

  def main(args: Array[String]): Unit = {

    //create a spark session, and run it on local mode
    val spark = SparkSession.builder()
      .appName("HelloStructuredStreaming")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    //read from a directory as text stream
    val readme: Dataset[String] = spark
      .readStream
      .textFile("/Users/vviswanath/Downloads/streaming_input_dir/cars/")

    //do word count
    val words = readme.flatMap(_.split(" "))
    val wordCounts = words.groupBy("value").count()

    //run the wordCount query and write to console
    val query = wordCounts
        .writeStream
        .queryName("WordCount")
        .outputMode("complete")
        .format("console")
        .start()

    //wait till query.stop() is called
    query.awaitTermination()
  }
}
