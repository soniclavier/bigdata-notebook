package com.vishnu.spark.streaming

import org.apache.spark.streaming.kafka._
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._

/**
 * @author : vishnu viswanath
 * Receiver based approach, i.e., used kafka consumer api to implement receiver
 * Drawback : possible loss of data incase of failures
 * 
 * Solution : use write-ahead logs and Reliable receivers.
 * Spark provides a built in ReliableKafkaReceiver class which is not used by default.
 * To use this receiver, set spark.streaming.receiver.writeAheadLog.enable to true 
 *  
 *  
 */
object KafkaStreaming {
  
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("KafkaStreaming").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf, Seconds(1))
    
    //default zookeeper quorum is localhost in single node setup
    val zqQuorum = "localhost"
    val groupId = "spark"
    val topics = "spark_streaming"
    val topicMap = topics.split(",").map((_, 1)).toMap
    val lines = KafkaUtils.createStream(ssc,zqQuorum,groupId,topicMap)
    val words = lines.map(_._2).flatMap(_.split(" "))
    val pairs = words.map(word => (word,1))
    val wordCounts = pairs.reduceByKey(_+_)
    wordCounts.print()
    
    ssc.start()
    ssc.awaitTermination()
  }
}