package com.vishnu.spark.streaming.akka

import org.apache.spark._
import org.apache.spark.streaming._
import akka.actor.Props
import akka.actor.Actor
import org.apache.spark.streaming.receiver.ActorHelper

class HelloSpark extends Actor with ActorHelper {

  override def preStart() = {
    println("")
    println("Starting HelloSpark Actor") 
    println("")
  }
  
  def receive = {
    case s => store(s)
  }
}


object SparkAkkaSource {
  
  val driverPort = 7777
  val driverHost = "localhost"
  val actorName = "HelloSparkActor"
    
  def main(args: Array[String]): Unit = {
    
    //fix a driver port, this is by default random (but now we need to know what driver port is)
    
    
    val conf = new SparkConf(false)
    .setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    .setAppName("Spark Streaming from Akka")
    .set("spark.logConf","true")
    .set("spark.driver.port",driverPort.toString)
    .set("spark.driver.host",driverHost)
    .set("spark.akka,logLifeCycleEvents","true")
    
    val ssc = new StreamingContext(conf,Seconds(1))
    
    val actorName ="helloSpark"
    val actorStream =ssc.actorStream[String](Props[HelloSpark], actorName)
    actorStream.print()
    
    ssc.start()
    ssc.awaitTermination()
    
    
  }
  
}