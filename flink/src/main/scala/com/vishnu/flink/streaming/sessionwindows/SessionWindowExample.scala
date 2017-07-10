package com.vishnu.flink.streaming.sessionwindows

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.joda.time
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

/**
  * Created by vviswanath on 6/3/17.
  *
  * An example for EvenTime based SessionWindows. To count the number of Clicks made by a User in a Session.
  * Makes use of AllowedLateness to anticipate late arriving Click events.
  */
object SessionWindowExample {

  def main(args: Array[String]): Unit = {
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    senv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    senv.setParallelism(1)

    val rawStream: DataStream[String] = senv.socketTextStream("localhost", 4444)
    val events: DataStream[Click] = rawStream.flatMap(Click(_))

    val eventsWithTs = events.assignTimestampsAndWatermarks(new WatermarkGenerator)

    val output: DataStream[(String, Int)] = eventsWithTs
        .keyBy("userId")
        .window(EventTimeSessionWindows.withGap(Time.seconds(2)))
        .allowedLateness(Time.seconds(4))
        .apply[(String, Int)](new WindowFunction[Click, (String, Int), Tuple, TimeWindow](){
          override def apply(key: Tuple,
                             window: TimeWindow,
                             input: Iterable[Click],
                             out: Collector[(String, Int)]): Unit = {
            out.collect((key.getField(0).toString, input.size))
          }
    })

    output.print()

    senv.execute("SessionWindowExample")
  }

  case class Click(timestamp: Long, userId: String, source: String) {
    override def toString: String = {
      s"Event(${new time.DateTime(timestamp).toString("mm:ss.SSS")}, $userId, $source)"
    }
  }

  object Click {
    def apply(raw: String): Option[Click] = {
      val p = raw.split(",")
      Try(Click(p(0).toLong, p(1), p(2))) match {
        case Success(e) ⇒ {
          println(s"received $e at ${new DateTime(System.currentTimeMillis).toString("mm:ss.SSS")}")
          Some(e)
        }
        case Failure(e) ⇒ {
          println(e)
          None
        }
      }
    }
  }

  class WatermarkGenerator extends AssignerWithPeriodicWatermarks[Click] {
    var ts = Long.MinValue
    override def getCurrentWatermark: Watermark = {
      new Watermark(ts)
    }

    override def extractTimestamp(t: Click, l: Long): Long = {
      ts = Math.max(t.timestamp, l)
      t.timestamp
    }
  }
}
