package com.vishnu.spark.graph

import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

object PropertyGraphExample {
  
  def main(args: Array[String]): Unit = {
    //confs
    val conf = new SparkConf().setAppName("AirportGraph")
    val sc = new SparkContext(conf)
    
    //load data
    val airports = sc.textFile("/mapr_lab_data/data/airports.csv").map(parseAirport)
    
    
  }
  
  case class Route(src:Int, dest:Int, dist: Int)
  case class Airport(id:Int, name:String)
  
  def parseRoute(str:String): Route = {
    val p = str.split(",")
    Route(p(0).toInt, p(1).toInt, p(2).toInt)
  }
  def parseAirport(str:String): Airport = {
    val p = str.split(",")
    Airport(p(0).toInt, p(1))
  }
}