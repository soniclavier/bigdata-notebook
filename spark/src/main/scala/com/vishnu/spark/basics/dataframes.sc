package com.vishnu.spark.basics

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.udf



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
		
		//auctionsDF.groupBy("itemtype", "aucid").count.agg(min("count"), avg("count"), max("count")).show
		
		auctionsDF.filter(auctionsDF("price")>150).show
		
		//second course
		val sfpd = sc.textFile("/user/vishnu/mapr/dev361/sfpd.csv").map(_.split(","))
		case class Incidents(incidentnum:String, category:String, description:String, dayofweek:String, date:String, time:String, pddistrict:String, resolution:String, address:String, X:Float, Y:Float, pdid:String)
		val sfpdCase = sfpd.map(x=>Incidents(x(0),x(1),x(2),x(3),x(4),x(5),x(6),x(7),x(8),x(9).toFloat,x(10).toFloat,x(11)))
		val sfpdDF = sfpdCase.toDF
		sfpdDF.registerTempTable("sfpd")
		
		val testsch = StructType(Array(StructField("IncNum",StringType,true),StructField("Date",StringType,true),
		StructField("District",StringType,true)))
		
		
		//depricated
		//val sfpdjson = sqlContext.load("/user/vishnu/mapr/dev361/sfpd.json","json")
		
		//correct way from spark 1.4
		val sfpdjson = sqlContext.read.format("json").load("/user/vishnu/mapr/dev361/sfpd.json")
		
		sfpdDF.groupBy("pddistrict").count.sort($"count".desc).show(5)
		sfpdDF.groupBy("resolution").count.sort($"count".desc).show(10)
		 val top10ResSQl = sqlContext.sql("SELECT resolution,count(incidentnum) as count from sfpd group by resolution order by count desc limit 10")
		
		//save
		//depricated
		top10ResSQl.toJSON.saveAsTextFile("/user/vishnu/mapr/dev361/top10Res.json")
		
		//correct way from spark 1.4
		top10ResSQl.write.format("json").mode("overwrite").save("/user/vishnu/mapr/dev361/top10Res.json")
		
		//creating udf
		def getStr = udf((s:String)=> {
     val lastS = s.substring(s.lastIndexOf('/')+1)
     lastS
     })
     
     
     //inline usage of udf
     val yy = sfpdDF.groupBy(getStr(sfpdDF("date"))).count.show
     
     
     //define function and register it as udf
     def getStr(s:String) = {
     	val strAfter = s.substring(s.lastIndexOf('/')+1)
     	strAfter
     }
     
     //register as udf
     sqlContext.udf.register("getStr",getStr _)
}