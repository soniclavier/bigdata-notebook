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
    val vertices = airports.map(airport => (airport.id.toLong,airport))  //note id.toLong, we need that for creating Graph, because Graph()'s first arg takes an RDD of tuples with _0 that has a Long 
    
    val routes = sc.textFile("/mapr_lab_data/data/routes.csv").map(parseRoute)
    val edges = routes.map(route => Edge(route.src, route.dest, route))
    
    //create defualt vertex
    val defaultVertex = Airport(0,"default")
    
    //create graph
    val graph = Graph(vertices, edges, defaultVertex)
    
    graph.vertices.collect.foreach(println)
    
    graph.triplets.collect.foreach(println)
    println(graph.inDegrees)
    println(graph.vertices.count())
    println(graph.edges.count())
    
    graph.edges.filter{case Edge(src,dest,route) => route.dist > 1000}.count
    graph.edges.filter{case Edge(src,dest,route) => route.dist > 1000}.collect.foreach(println)
    
    graph.triplets.sortBy(_.attr,ascending=false).collect.foreach(println)
    
    
    //page rank
    val ranks = graph.pageRank(0.1).vertices
    ranks.take(3)
    
    ranks.join(vertices).sortBy(_._2._1,false).map(_._2._2).collect.foreach(println)
  }
  
  case class Route(src:Int, dest:Int, dist: Int) 
  object Route{
    
    implicit def orderingByDist[A <: Route]: Ordering[A] =
      Ordering.by(r => (r.dist))
  }
  case class Airport(id:Int, name:String)
  
  def parseRoute(str:String): Route = {
    val p = str.split(",")
    new Route(p(0).toInt, p(1).toInt, p(2).toInt)
  }
  def parseAirport(str:String): Airport = {
    val p = str.split(",")
    Airport(p(0).toInt, p(1))
  }
}