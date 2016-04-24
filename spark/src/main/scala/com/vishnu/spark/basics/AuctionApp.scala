package com.vishnu.spark.basics

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext

object AuctionApp {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("AuctionsApp")
    val sc = new SparkContext(conf)
    
    val aucFile = "/user/vishnu/mapr/dev360/auctiondata.csv"
    val auctionRDD = sc.textFile(aucFile).map(_.split(",")).cache()
  }
}