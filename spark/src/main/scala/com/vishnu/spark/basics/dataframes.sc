package com.vishnu.spark.basics

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext



object dataframes {

		println("Welcome to the Scala worksheet")
	  val conf = new SparkConf().setAppName("dataframe-test").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
		val sc = new SparkContext(conf)
		val sqlContext = new SQLContext(sc)
		
		import sqlContext.implicits._
		
		case class Auctions(aucid:String, bid:Float,bidtime:Float,bidder:String,bidrate:Int,openbid:Float, price:Float,itemtype:String,dtl:Int)
		
		val auctionRDD = sc.textFile("/user/vishnu/mapr/dev360/auctiondata.csv").map(_.split(","))
		val auctions = auctionRDD.map(a=>Auctions(a(0),a(1).toFloat,a(2).toFloat,a(3),a(4).toInt,a(5).toFloat,a(6).toFloat,a(7),a(8).toInt))
		val auctionsDF = auctions.toDF()
		auctionsDF.registerTempTable("auctionsDF")
		
		auctionsDF.groupBy("itemtype", "aucid").count.agg(min("count"), avg("count"), max("count")).show
		
		auctionsDF.filter(auctionsDF("price")>150).show
}