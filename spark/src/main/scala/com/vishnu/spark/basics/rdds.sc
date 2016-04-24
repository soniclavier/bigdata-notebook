package com.vishnu.spark.basics

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf


object rdds {

	
  println("Welcome to the Scala worksheet")
  
  val conf = new SparkConf().setAppName("rdd-test").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
	val sc = new SparkContext(conf)
	//define RDD
  val auctionRDD = sc.textFile("/user/vishnu/mapr/dev360/auctiondata.csv").map(_.split(","))
  
 	//filter transformation, applying anonymous function
  val xboxRDD = auctionRDD.filter(line => line.contains("xbox"))
  
  val auctionid = 0
  val bid = 1
  val bidtime = 2
  val bidder = 3
  val bidderrate = 4
  val openbid = 5
  val price = 6
  val itemtype = 7
  val daystolive = 8
  
  //how many items where sold
  val items_sold = auctionRDD.map(entry=>entry(auctionid))
  .distinct
  .count
  
  //how many bids per item type
  val bidAuctionRDD = auctionRDD.map(entry=>(entry(itemtype),1)).reduceByKey((x,y)=>x+y)
  
  //cache
  bidAuctionRDD.cache
  
  bidAuctionRDD.collect
  
   
  
  
}