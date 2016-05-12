package com.vishnu.spark.streaming

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._


/**
 * Example from spark programming guide
 * https://spark.apache.org/docs/1.4.1/streaming-programming-guide.html
 */
object StreamingWithCheckpointing {
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("StreamingWithCheckpointing").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint("hdfs:///user/vishnu/spark_checkpoint")
    
    val lines = ssc.socketTextStream("localhost", 9999)
    val words = lines.flatMap(_.split(" "))
    val pairs = words.map(word => (word,1))
    
    pairs.checkpoint(Seconds(10));
    val wordCounts = pairs.reduceByKey(_+_)
    
    wordCounts.print()
    
    ssc.start()
    ssc.awaitTermination()
  }
}