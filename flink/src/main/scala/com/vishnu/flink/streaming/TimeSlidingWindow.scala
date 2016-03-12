package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * A sliding window based on time. In contrast to Tumbling window a sliding is an overlapping window.
 */
object TimeSlidingWindow {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)
    
    //the following window is triggered every 10 seconds,for last 15 seconds data
    //therefore there is an overlap between data being processed at an instance and previous processing.
    val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .timeWindow(Time.seconds(15),Time.seconds(10))
      .sum(1).setParallelism(4);
    
    counts.print()
    sev.execute()
  }
}