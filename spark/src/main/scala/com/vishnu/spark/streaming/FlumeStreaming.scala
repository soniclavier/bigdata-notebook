package com.vishnu.spark.streaming

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.flume._
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions


object FlumeStreaming {
  def main(args: Array[String]) {
    
    val host = "localhost"
    val port = 4444
    val conf = new SparkConf().setAppName("FlumeStreaming").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf, Seconds(1))
    
    val stream = FlumeUtils.createStream(ssc, host, port)
    val words = stream.flatMap(_.event.toString().split(" "))
    val pairs = words.map(word => (word,1))
    val wordCounts = pairs.reduceByKey(_+_)
    
    wordCounts.print()
    
    ssc.start()
    ssc.awaitTermination()
  }
}