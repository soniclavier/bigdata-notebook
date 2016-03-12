package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.evictors.TimeEvictor
import java.util.concurrent.TimeUnit

class WindowWithTrigger {
     def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)
    
    //the following window is triggered for every 15
    /*val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .window(SlidingEventTimeWindows.of(Time.seconds(5),Time.seconds(1))
      .evictor(TimeEvictor.of(Time.of(10, TimeUnit.MILLISECONDS)))
      .sum(1).setParallelism(4);
    
    counts.print()
    sev.execute()*/
  }
}