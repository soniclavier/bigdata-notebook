package com.vishnu.flink.streaming

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.util.Collector
import org.joda.time.format.DateTimeFormat

object EventTimeWindowWithTrigger {
     def main(args: Array[String]) {
       val sev = StreamExecutionEnvironment.getExecutionEnvironment
       sev.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
       val rawMessages: DataStream[String] = sev.socketTextStream("localhost",4444)

       val coloredMessagesStream: DataStream[ColoredMessage] = rawMessages.flatMap(new FlatMapFunction[String,ColoredMessage] {
         override def flatMap(value: String, out: Collector[ColoredMessage]): Unit = {
           out.collect(ColoredMessage(value.split(",")))
         }
       })




    sev.execute()
  }
}
case class ColoredMessage(eventTime: Long, color: String)

object ColoredMessage {
  def apply(parts: Array[String]): ColoredMessage = {
    ColoredMessage(
      eventTime = getDate(parts(0)),
      color = parts(1))
  }
  def getDate(date: String): Long = {
    val formatter = DateTimeFormat.forPattern("HH:mm:ss")
    val dt = formatter.parseDateTime(date)
    dt.getMillis
  }
}

class TimestampAndWatermarkGen extends AssignerWithPeriodicWatermarks[ColoredMessage] {
  val maxDelay = 1*60*1000 //1 minute
  var maxTime = 0L
  override def getCurrentWatermark: Watermark = {
    new Watermark(maxTime - maxDelay)
  }
  override def extractTimestamp(element: ColoredMessage, previousElementTimestamp: Long): Long = {
    maxTime = Math.max(element.eventTime, maxTime)
    return element.eventTime
  }
}

