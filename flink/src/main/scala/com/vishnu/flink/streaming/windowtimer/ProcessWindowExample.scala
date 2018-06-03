//package com.vishnu.flink.streaming.windowtimer
//
//
//import com.vishnu.flink.streaming.TimestampAndWatermarkGen
//import org.apache.flink.api.java.tuple.Tuple
//import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
//import org.apache.flink.streaming.api.scala.function.{ProcessAllWindowFunction, ProcessWindowFunction, RichWindowFunction}
//import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
//import org.apache.flink.streaming.api.watermark.Watermark
//import org.apache.flink.streaming.api.windowing.time.Time
//import org.apache.flink.streaming.api.windowing.windows.{TimeWindow, Window}
//import org.apache.flink.util.Collector
//import org.apache.flink.api.scala._
//import org.apache.flink.streaming.api.TimeCharacteristic
//
//
///**
//  * Created by vviswanath on 4/16/17.
//  */
//object ProcessWindowExample {
//
//  def main(args: Array[String]): Unit = {
//    val senv = StreamExecutionEnvironment.getExecutionEnvironment
//    senv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//
//    val rawInput: DataStream[String] = senv.fromCollection(Array("k1\tv1\t1495767605556", "k2\tv2\t1495767614349", "k3\tv3\t1495767627018"))
//
//    val input = rawInput.map(s ⇒ Message(s.split("\t")))
//
//    val inputWithTs: DataStream[Message] = input.assignTimestampsAndWatermarks(new TimestampAndWatermarkExtractor)
//
//    inputWithTs
//      .keyBy("key")
//      .timeWindow(Time.seconds(5))
//      .process[String](new ProcessWindowFunction[Message, String, Tuple, TimeWindow] {
//
//      override def onTimer(time: Long, context: OnTimerContext, out: Collector[String]): Unit = {
//        println("timer triggered")
//        println(context.timeDomain)
//      }
//
//      override def process(key: Tuple, context: Context, elements: Iterable[Message], out: Collector[String]): Unit = {
//        //context.registerEventTimeTimer(100000)
//        context.registerProcessingTimeTimer(10000)
//        println("timer registered")
//      }
//    })
//
//
//    senv.execute("test")
//  }
//
//  case class Message(key: String, value: String, ts: String)
//
//  object Message {
//    def apply(arr: Array[String]): Message = {
//      this(arr(0), arr(1), arr(2))
//    }
//  }
//
//  class TimestampAndWatermarkExtractor extends AssignerWithPeriodicWatermarks[Message] {
//    var currentLowWatermark = Long.MinValue
//    override def getCurrentWatermark: Watermark = new Watermark(currentLowWatermark)
//    override def extractTimestamp(t: Message, l: Long): Long =  {
//      val ts = t.ts.toLong
//      currentLowWatermark = math.min(ts, currentLowWatermark)
//      ts
//    }
//  }
//
//}
//
