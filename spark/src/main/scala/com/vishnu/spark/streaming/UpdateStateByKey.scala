package com.vishnu.spark.streaming

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._


/**
 * An example of update state by key
 * Each log entry contains <userid> <action>
 * based on the action, state of the userid is udpated
 */
object UpdateStateByKey {
  def main(args: Array[String]) {
    
    
    val conf = new SparkConf().setAppName("UpdateStateByKey").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint("hdfs:///user/vishnu/spark_checkpoint")
    
    
    val linesDStream = ssc.socketTextStream("localhost", 9999)
    
    //input is expected to be of the format <userid> <action>
    val userActionPair = linesDStream.map(line => {
      val parts = line.split(" ")
      (parts(0),parts(1))
    })
    
    val userStates = userActionPair.updateStateByKey(updateUserState)
    userStates.print()
    
    ssc.start()
    ssc.awaitTermination()
  }
  
  def updateUserState(values: Seq[String], state:Option[String]) = {
    val currState = state.getOrElse("Unknown")
    var newState = Option(currState)
    if (!currState.equals(values.lastOption)) {
      if (values.lastOption != None) {
        newState = values.lastOption
      }
    }
    newState
  }
}