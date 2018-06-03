package com.vishnu.flink.streaming.cep

import java.util.Properties

import com.vishnu.flink.util.ParameterParser
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.cep.nfa.AfterMatchSkipStrategy
import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

import scala.collection.Map


/**
  * Created by vviswanath on 3/19/18.
  * A FLINK CEP example that creates Alerts based on Pattern.
  *
  * sorting of records (buffer for how long?) - till watermark
  * how to set allowed lateness in CEP
  */

case class ServerEvent(ts: Long, eventType: String, serverIp: String, value: Double)

object ServerEvent {
  val CPU_USAGE = "cpu-usage"
  val MEM_USAGE = "mem-usage"
  val DISK_USAGE = "disk-usage"
}

class ParseRawEvent extends FlatMapFunction[String, ServerEvent] {
  override def flatMap(value: String, out: Collector[ServerEvent]): Unit = {
    try {
      val parts = value.split(",")
      val event = ServerEvent(parts(0).toLong, parts(1), parts(2), parts(3).toDouble)
      out.collect(event)
    } catch  {
      case e: Exception ⇒ {
        println(s"unable to parse $value due to ${e.printStackTrace()}\n")
      }
    }
  }
}

case class Alert(message: String)
object Alert {
  def apply(params: Map[String, Iterable[ServerEvent]]): Alert = {
    val serverIp = params("start").head.serverIp
    Alert(s"Health check for server $serverIp")
  }
}
object HelloCep {
  def main(args: Array[String]): Unit = {
    val params = ParameterParser.parse(args)
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    senv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val props = new Properties()
    props.put("bootstrap.servers", params.getOrElse("kafka-broker", "localhost:9092"))

    val kafkaConsumer = new FlinkKafkaConsumer011[String](
      "server_events",
      new SimpleStringSchema(),
      props)

    val serverEventsStream: DataStream[ServerEvent] = senv
      .addSource[String](kafkaConsumer)
      .flatMap[ServerEvent](new ParseRawEvent)
      .assignTimestampsAndWatermarks(new ServerEventTsAndWatermarkExtractor(Time.seconds(5)))
      .keyBy(se ⇒ se.serverIp)

    val skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent()

    val pattern = Pattern
      .begin[ServerEvent]("start", skipStrategy)
      .where(se ⇒ se.eventType.equals(ServerEvent.CPU_USAGE) && se.value > 90)
      .timesOrMore(5)
      .followedBy("high-mem")
      .where(se ⇒ se.eventType == ServerEvent.MEM_USAGE && se.value > 90)
      .timesOrMore(4).greedy
      .within(Time.seconds(10))

    val patternStream = CEP.pattern[ServerEvent](serverEventsStream, pattern)

    val alertStream = patternStream.select(Alert(_))

    alertStream.print()

    senv.execute()

  }
}

class ServerEventTsAndWatermarkExtractor(maxDelay: Time = Time.seconds(0)) extends BoundedOutOfOrdernessTimestampExtractor[ServerEvent](maxDelay) {
  override def extractTimestamp(element: ServerEvent): Long = element.ts
}