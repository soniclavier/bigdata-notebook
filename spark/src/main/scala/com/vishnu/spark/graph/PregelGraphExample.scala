package com.vishnu.spark.graph

import org.apache.spark._

import org.apache.spark.rdd.RDD
import org.apache.spark.util.IntParam
import org.apache.spark.graphx._
import org.apache.spark.graphx.util.GraphGenerators

object PregelGraphExample {

  def main(args: Array[String]): Unit = {
    //confs
    val conf = new SparkConf().setAppName("AirportGraph")
    val sc = new SparkContext(conf)

    val textRDD = sc.textFile("/mapr_lab_data/data/rita2014jan.csv")
    val flightsRDD = textRDD.map(parseFlight).cache

    val airports = flightsRDD.map(flight => (flight.org_id, flight.origin)).distinct
    val nowhere = "nowhere"

    val airportMap = airports.map { case ((org_id), name) => (org_id -> name) }.collect.toList.toMap

    val routes = flightsRDD.map(flight => ((flight.org_id, flight.dest_id), flight.dist)).distinct
    val edges = routes.map { case ((src, dest), dist) => Edge(src.toLong, dest.toLong, dist) }

    val graph = Graph(airports, edges, nowhere)

    val numAirpots = graph.vertices.count
    val numRoutes = graph.edges.count
    graph.edges.filter { case (Edge(src, dst, dist)) => dist > 1000 }
    graph.triplets.take(3).foreach(println)

    graph.inDegrees.reduce(max)
    graph.outDegrees.reduce(max)
    graph.degrees.reduce(max)

    val maxIncoming = graph.inDegrees.collect.sortWith(_._2 > _._2).map(x => (airportMap(x._1), x._2))
    val ranks = graph.pageRank(0.1).vertices
    ranks.join(airports).sortBy(_._2._1, false).map(_._2._2).take(4)

    
    
    //compute cheapest fare
    //starting vertex
    val sourceId: VertexId = 13024
    
    //fare formula 50+dist/20
    //an Edge has this strucutre (src,dest,distance).
    //this is converted to (src,dest,fare)
    val gg = graph.mapEdges(e => 50.toDouble + e.attr.toDouble / 20)
    
    //vertex in gg/graph is of the form (id,name) => this is converted to (id,double value) 
    val initialGraph = gg.mapVertices((id, _) => if (id == sourceId) 0.0 else Double.PositiveInfinity)

    //Double.PositiveInfinity - initial message?
    val sssp = initialGraph.pregel(Double.PositiveInfinity)(
      (id, dist, newDist) => math.min(dist, newDist), //Vertex program, based on the incoming message, a property is computed
      triplet => { // message is sent to all neighbouring vertices 
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b) // Merge Message
      )

  }

  def max(a: (VertexId, Int), b: (VertexId, Int)): (VertexId, Int) = {
    if (a._2 > b._2) a else b
  }

  case class Flight(dofM: String, dofW: String, carrier: String, tailnum: String, flnum: Int, org_id: Long, origin: String, dest_id: Long, dest: String, crsdeptime: Double, deptime: Double, depdelaymins: Double, crsarrtime: Double, arrtime: Double, arrdelay: Double, crselapsedtime: Double, dist: Int)

  def parseFlight(str: String): Flight = {
    val line = str.split(",")
    Flight(line(0), line(1), line(2), line(3), line(4).toInt, line(5).toLong, line(6), line(7).toLong, line(8), line(9).toDouble, line(10).toDouble, line(11).toDouble, line(12).toDouble, line(13).toDouble, line(14).toDouble, line(15).toDouble, line(16).toInt)
  }
}