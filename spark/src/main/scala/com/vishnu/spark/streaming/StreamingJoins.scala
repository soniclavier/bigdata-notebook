package com.vishnu.spark.streaming

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._


/**
 * An example with multiple receivers and stream joins
 * 
 */
object StreamingJoins {
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("StreamingJoins").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf, Seconds(10))
    
    val stream1 = ssc.socketTextStream("localhost", 9999)
    val stream2 = ssc.socketTextStream("localhost", 8888)
    
    
    val words1 = stream1.map(processLine)
    val words2 = stream2.map(processLine)
    val joined = words1.join(words2)
    joined.print()
    
    ssc.start()
    ssc.awaitTermination()
  }
  
  def processLine(line:String) = {
    val words = line.split(" ")
      (words(0),line)
  }
}