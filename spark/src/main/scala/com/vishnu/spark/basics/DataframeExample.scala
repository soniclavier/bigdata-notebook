package com.vishnu.spark.basics

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/**
 * Data from https://archive.ics.uci.edu/ml/machine-learning-databases/housing/housing.data
 */
object DataframeExample {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("HouseData")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    import sqlContext.implicits._
    
    val houseData = sc.textFile("/spark_learning/housing.data.txt").map(x => {
      val parts = x.trim.split("[ ]+")
      val crime = if (parts(0).trim.equals("")) -1 else parts(0).toDouble
      val zone = if (parts(1).trim.equals("")) -1 else parts(1).toDouble
      val rooms = if (parts(5).trim.equals("")) -1 else parts(5).toDouble
      val age = if (parts(6).trim.equals("")) -1 else parts(6).toDouble
      HouseInfo(crime, zone, rooms, age)
    })

    val houseDf = houseData.toDF
  }

  case class HouseInfo(crime: Double, zone: Double, rooms: Double, age: Double)
}