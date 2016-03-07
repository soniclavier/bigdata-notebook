package com.vishnu.spark.streaming


import org.apache.spark._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.kafka.KafkaUtils
import kafka.serializer.StringDecoder



/**
 * DirectStream approach periodically queries the kafka topic for new offset and takes in data
 * from previous offset to new offset as an RDD
 * 
 * 1. creates as many RDD partitions as there are kafka partitions
 * 2. no need of write ahead log to ensure no data loss
 * 3. no zookeeper hence hence exactly-once guarantee can be maintained. In the case of zookeeper
 * 			there might some miss communication b/w spark and zookeeper during failures and chances are
 * 			there that some data may be read twice.
 * 
 */
object KafkaDirectStream {
  
  def main(args :Array[String]) {
    val conf = new SparkConf().setAppName("KafkaStreaming").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf,Seconds(1))
    val topics = "spark_streaming"
    val topicsSet = topics.split(",").toSet
    val brokers = "localhost:9092"
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    val lines = messages.map(_._2)
    
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
    wordCounts.print()

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
    
  }
}