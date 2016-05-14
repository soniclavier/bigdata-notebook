package com.vishnu.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds


object StreamingFromCheckpoint {
  
  val checkpoint_dir = "/user/vishnu/spark_checkpoint"
  var dataDir = ""
  def main(args: Array[String]): Unit = {
    dataDir = args(0)
    val ssc = StreamingContext.getOrCreate(checkpoint_dir,createStreamingContext _)
    
    ssc.start()
    ssc.awaitTermination() 
  }
  
  def createStreamingContext() = {
    println("creating new stream")
    val conf = new SparkConf().setAppName("StreamingFromCheckpoint")
    val ssc = new StreamingContext(conf,Seconds(3))
    ssc.checkpoint(checkpoint_dir)
    val dataDirDStream = ssc.textFileStream(dataDir)
    dataDirDStream.print()
    ssc
  }
}