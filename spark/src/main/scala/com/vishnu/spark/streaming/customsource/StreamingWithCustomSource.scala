package com.vishnu.spark.streaming.customsource

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds

object StreamingWithCustomSource {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("StreamingWithCustomSource")
    val ssc = new StreamingContext(conf,Seconds(5))
    
    val activityDStream = ssc.receiverStream(new ActivityReceiver(9999))
    activityDStream.print()
    
    ssc.start()
    ssc.awaitTermination()
    
  }
}