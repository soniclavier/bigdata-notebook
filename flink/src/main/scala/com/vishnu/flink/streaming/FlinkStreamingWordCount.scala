package com.vishnu.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._



object FlinkStreamingWordCount {
  
  def main(args: Array[String])  {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTxtStream = sev.socketTextStream("localhost",4444)
    
    val counts = socTxtStream.flatMap{_.split(" ")}
      .map { (_, 1) }
      .keyBy(0)
      .sum(1)
    counts.print()
    sev.execute()
  }
}