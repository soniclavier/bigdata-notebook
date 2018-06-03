//package com.vishnu.flink.streaming.windowtimer
//
//import java.lang.Iterable
//
//import org.apache.flink.api.java.tuple.Tuple
//import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction
//import org.apache.flink.streaming.api.windowing.windows.{TimeWindow, Window}
//import org.apache.flink.util.Collector
//
///**
//  * Created by vviswanath on 4/16/17.
//  */
//class ProcWindowFun[IN, OUT, K, W <: Window] extends ProcessWindowFunction[String, String, Tuple, TimeWindow] {
//  override def process(key: Tuple, context: ProcessWindowFunction[String, String, Tuple, TimeWindow]#Context, iterable: Iterable[String], collector: Collector[String]): Unit = {
//    context.registerEventTimeTimer(100)
//  }
//
//  override def onTimer(t: Long, context: ProcessWindowFunction[String, String, Tuple, TimeWindow]#OnTimerContext, out: Collector[String]): Unit = {
//    println("Timer triggered")
//  }
//}
