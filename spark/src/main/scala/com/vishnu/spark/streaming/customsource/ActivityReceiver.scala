package com.vishnu.spark.streaming.customsource

import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.storage.StorageLevel
import scala.util.control.Breaks._
import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader


case class Activity(user: String,action:String)

/**
 * Based on https://www.mapr.com/blog/how-integrate-custom-data-sources-apache-spark?platform=hootsuite
 */
class ActivityReceiver(port:Int) extends Receiver[Activity] (StorageLevel.MEMORY_ONLY){
  
  override def onStart(): Unit = {
    println("Activity Receiver starting")
    val thread = new Thread("ActivityReceiverThread") {
      override def run() {
        val socket = new Socket("localhost",port)
        val reader = new BufferedReader(new InputStreamReader (socket.getInputStream(), "UTF-8"))
        var line = ""
        while(!isStopped()) {
          var line = reader.readLine()
          if (line == null) break
          else {
            val parts = line.split(" ")
            val activity = Activity(parts(0),parts(1))
            store(activity)
          }
        }
      }
    }
    thread.start()
  }
  
  override def onStop(): Unit = {
    stop("Activity receiver stopping")
  }
}