package com.vishnu.spark.streaming

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.hadoop.io.Text
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat


/**
 * An example of how to stream from sequence file
 */
object SeqFileStreaming {
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("SeqFileStreaming").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf, Seconds(10))
    val inputDir = "/usr/vishnu/spark_temp/seqfile_sample/"
    val keyValue = ssc.fileStream[Text,IntWritable, SequenceFileInputFormat[Text,IntWritable]](inputDir).map {
      case (x,y) => (x.toString,y.get())
    }
    keyValue.print()
    
    ssc.start()
    ssc.awaitTermination()
  }
}