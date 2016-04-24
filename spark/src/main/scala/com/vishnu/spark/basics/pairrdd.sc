package com.vishnu.spark.basics

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext

object pairrdd {
  println("Welcome to the Scala worksheet")

  val IncidntNum = 0
  val Category = 1
  val Descript = 2
  val DayOfWeek = 3
  val Date = 4
  val Time = 5
  val PdDistrict = 6
  val Resolution = 7
  val Address = 8
  val X = 9
  val Y = 10
  val PdId = 11
  
  val conf = new SparkConf().setAppName("pairrdd-test").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
	val sc = new SparkContext(conf)
	
	val sfpd = sc.textFile("/user/vishnu/mapr/dev361/sfpd.csv").map(_.split(","))
	val totincs = sfpd.count()
	val cat = sfpd.map(x=>x(Category)).distinct.collect()
	
	val bayviewRDD = sfpd.filter(incident=>incident.contains("BAYVIEW"))
	
	val incByCat = sfpd.map(x=>(x(Category),1))
	
	sfpd.map(x=>(x(PdDistrict),1)).reduceByKey(_+_).map(x=>(x._2,x._1)).sortByKey(false).take(4)
	
	val pdDists = sfpd.map(x=>(x(PdDistrict),x(Address)))
	val catRes = sfpd.map(x=>(x(PdDistrict),(x(Category),x(Resolution))))
	val incCatRes = sfpd.map(x=>(x(PdDistrict),x(Address)))
	
	pdDists.join(catRes)
	
	// only if dataset can fit in memory
	val num_inc_dist = sfpd.map(x=>(x(PdDistrict),1)).countByKey()
	
	val catAdd = sc.textFile("/user/vishnu/mapr/dev361/J_AddCat.csv").map(x=>x.split(",")).map(x=>(x(1),x(0)))
	
}