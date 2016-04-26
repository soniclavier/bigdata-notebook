package com.vishnu.spark.basics

import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._

object streams {
  println("Welcome to the Scala worksheet")

  val sparkConf = new SparkConf().setAppName("SensorStream")
  val sc = new SparkContext(sparkConf)
  case class Sensor(resid: String, date: String, time: String, hz: Double, disp: Double, flo: Double, sedPPM: Double, psi: Double, chlPPM: Double) extends Serializable

  val ssc = new StreamingContext(sc, Seconds(2))
  val linesDStream = ssc.textFileStream("/user/user01/stream")
  linesDStream.print()
  linesDStream.foreachRDD(rdd => {
    val srdd = rdd.map(_.split(",")).map(p => Sensor(p(0), p(1), p(2), p(3).toDouble, p(4).toDouble, p(5).toDouble, p(6).toDouble, p(7).toDouble, p(8).toDouble))
    val alertRDD = srdd.filter(sensor=>sensor.psi < 5.0)
    srdd.take(2).foreach(println)
    alertRDD.take(2).foreach(println)
  })
  

  ssc.start()
  ssc.awaitTermination()
  

}


  
  