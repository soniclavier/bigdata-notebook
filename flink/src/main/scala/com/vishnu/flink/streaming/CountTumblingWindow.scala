package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


/**
 * A tumbling window based on count
 */
object CountTumblingWindow {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)
    
    //the following window is triggered for every 5 items
    //since we are doing keyby
    //each window will be containing only words of the same group
    //e.g.,
    //if stream is : one two one two one two one two one
    //window1 = {one,one,one,one,one}
    //window2 = {two,two,two,two}
    //window1 will triggered but not window 2, it need one more 'two' to make it 5
    val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .countWindow(5)
      .sum(1).setParallelism(4);
    
    counts.print()
    sev.execute()
  }
}