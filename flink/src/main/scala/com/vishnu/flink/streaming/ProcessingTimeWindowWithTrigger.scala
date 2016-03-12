package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.{SlidingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.evictors.{CountEvictor}

import org.apache.flink.streaming.api.windowing.triggers.CountTrigger

object ProcessingTimeWindowWithTrigger {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)

    //a window of size 20 seconds is created, window slides every 10 seconds
    //execution of window is triggered when there are 3 elements in the window
    val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .window(SlidingProcessingTimeWindows.of(Time.seconds(20),Time.seconds(10)))
      .trigger(CountTrigger.of(3))
      .sum(1).setParallelism(4);

    counts.print()
    sev.execute()
  }
}
