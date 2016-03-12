package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, GlobalWindows}
import org.apache.flink.streaming.api.windowing.evictors.{CountEvictor, TimeEvictor}
import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.windowing.triggers.CountTrigger

  object EventTimeWindowWithTrigger {
     def main(args: Array[String]) {
       val sev = StreamExecutionEnvironment.getExecutionEnvironment
       val socTextStream = sev.socketTextStream("localhost",4444)

       //a window of size 10 seconds is created, window slides every 3 seconds
       //execution of window is done when there are 10 elements
       //once it is executed, 3 items are kept in the window, rest is evicted.
       val counts = socTextStream.flatMap{_.split("\\s")}
         .map { (_, 1) }
         .keyBy(0)
         .window(SlidingEventTimeWindows.of(Time.seconds(10),Time.seconds(3)))
         .trigger(CountTrigger.of(10))
         .evictor(CountEvictor.of(3))
         .sum(1).setParallelism(4);

    counts.print()
    sev.execute()
  }
}