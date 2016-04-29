package com.vishnu.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.api._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext

object StreamingWindow {

  def main(args: Array[String]): Unit = {
    //confs
    val conf = new SparkConf().setAppName("StreamingWindow")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(1))
    
    //stream from text file
    val linesDStream = ssc.textFileStream("/user/vishnu/mapr/dev362");
    val sensorDStream = linesDStream.map(parseSensor)
    
    //count of events by resid
    val counts = sensorDStream.map(sensor=>(sensor.resid,1)).reduceByKeyAndWindow((a:Int,b:Int)=>(a+b), Seconds(6), Seconds(2))
    counts.print()
    
    //6 seconds data, 2 seconds window
    sensorDStream.window(Seconds(6),Seconds(2)).foreachRDD {
      rdd =>
        if (!rdd.partitions.isEmpty) {
          val sqlContext = SQLContext.getOrCreate(rdd.sparkContext)
          import sqlContext.implicits._
          import org.apache.spark.sql.functions._
          
          val sensorDF = rdd.toDF()
          sensorDF.registerTempTable("sensor")
          
          val res = sqlContext.sql("SELECT resid, date, count(resid) as total FROM sensor GROUP BY resid, date")
          println("sensor count ")
          res.show
          val res2 = sqlContext.sql("SELECT resid, date, MAX(psi) as maxpsi, min(psi) as minpsi, avg(psi) as avgpsi FROM sensor GROUP BY resid,date")
          println("sensor max, min, averages ")
          res2.show
        }
    }
    
    
    println("Starting streaming")
    ssc.start()
    ssc.awaitTermination()
   
    
  }
  
  case class Sensor(resid: String, date: String, time: String, hz: Double, disp: Double, flo: Double, sedPPM: Double, psi: Double, chlPPM: Double) extends Serializable
  
  def parseSensor(str: String): Sensor = {
    val p = str.split(",")
    Sensor(p(0), p(1), p(2), p(3).toDouble, p(4).toDouble, p(5).toDouble, p(6).toDouble, p(7).toDouble, p(8).toDouble)
  }
}