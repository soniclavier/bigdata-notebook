package com.vishnu.spark.streaming.akka

import org.apache.spark._
import akka.actor.ActorSystem

/**
 * This object is used to send message to HelloSpark akka Actor
 */
/*
object SendToActor {
  
  def main(args: Array[String]) : Unit = {
    val actorSystem = ActorSystem("sparkMaster")
    
    val url = s"akka.tcp://sparkDriver@$SparkAkkaSource.driverHost:$SparkAkkaSource.driverPort/user/Supervisor0/$SparkAkkaSource.actorName"
    val helloer = actorSystem.actorSelection(url)
    
    var ok = true
    while (ok) {
      val ln = readLine()
      ok = ln != null
      if (ok) {
        helloer ! ln
      }
    }
  }
  
}*/