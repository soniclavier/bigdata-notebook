package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


/**
 * A tumbling window based on time
 */

object TimeTumblingWindow {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)
    
    //the following window is triggered every 15 seconds.
    val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .timeWindow(Time.seconds(15))
      .sum(1).setParallelism(4);
    
    counts.print()
    sev.execute()
  }
}