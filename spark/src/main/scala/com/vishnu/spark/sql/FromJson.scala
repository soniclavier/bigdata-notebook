package com.vishnu.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object FromJson {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SparkSQLBasics")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    val input = sqlContext.read.json("/spark_learning/testweet.json")
    
    input.registerTempTable("tweets")
    val texts = sqlContext.sql("select text from tweets")
    
    
    //udf register
    sqlContext.udf.register("strLen",(x:String)=>{findLength(x)})
    texts.foreach(println)
  }
  
  def findLength(x:String) = {
    x.length
  }
}