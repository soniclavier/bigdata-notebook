package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


/**
 * A tumbling window based on count
 */
object TumblingWindowStreamingCount {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)
    
    //the following window is triggered for every 10 events, collecting last 15 events
    val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .countWindow(15,10)
      .sum(1).setParallelism(4);
    
    counts.print()
    sev.execute()
  }
}