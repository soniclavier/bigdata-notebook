package com.vishnu.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext

object HiveTest {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SparkSQLBasics")
    val sc = new SparkContext(conf)
    
    val sqlContext = new HiveContext(sc)
    
    val input = sqlContext.read.json("/spark_learning/testweet.json")
    input.saveAsTable("tweets")
  }
}