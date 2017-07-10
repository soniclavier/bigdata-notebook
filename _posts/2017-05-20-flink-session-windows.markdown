---
layout: post
comments: true
title: Session Windows in Apache Flink
date: 2017-06-10
PAGE_IDENTIFIER: flink_session_windows
permalink: /flink-session-windows.html
image: /img/flink_session_windows/logo.png
tags: ApacheFlink BigData Hadoop Scala Streaming
description: Apache Flink's Session Windows allows messages to be windowed into sessions. In this blog, we will create a streaming application that counts number of Clicks made by each user within a session using EventTimeSession windows.
---
<div class="col three">
    <img class="col three" src="/img/flink_session/header.png">
</div>
Session Windows in Apache Flink allows messages to be [Windowed](flink_streaming) into sessions based on user's activity. Flink allows us to define a time gap and all the messages that arrive within a "period of inactivity" less than the defined time gap can be considered to belong to the same session. This has many practical use cases, mainly because this relates to Sessions in Web applications.

In this blog, we will build a streaming application that uses [EventTime](flink_eventtime) based Session Windows to identify how many times user made a Click during a session. Flink version at the time of writing this blog is 1.3.0. All the code used in this blog can be found in my [Github]() <i class="fa fa-github" aria-hidden="true"></i>.
<div class="col three">
    <img class="col three expandable" src="/img/flink_session/use_case.png">
</div>
### Message format
For this example, our Click events are of the format <timestamp, user_id, event_source>, where event_source can be recommendation, ad etc. This following case class can be used to capture these messages.
{% highlight scala %}
case class Click(timestamp: Long, userId: String, source: String)
{% endhighlight %}
We could also create a companion object to make it easy to parse the raw logs into Clicks.
{% highlight scala %}
object Click {
    def apply(raw: String): Option[Click] = {
      val p = raw.split(",")
      Try(Click(p(0).toLong, p(1), p(2))) match {
        case Success(e) ⇒ {
          Some(e)
        }
        case Failure(e) ⇒ {
          None
        }
      }
    }
  }
{% endhighlight %}

### Streaming Pipeline
Next, we create the pipeline. We will be creating an EventTime based application since messages can come delayed and we should be able to handle such scenarios. If you are not aware of the terms EventTime, ProcessingTime and Watermarks please read this [blog](flink_eventtime) post.
{% highlight scala %}
val senv = StreamExecutionEnvironment.getExecutionEnvironment
senv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

//read the raw_logs from socket and parse into DataStream[Click] events.
val rawStream: DataStream[String] = senv.socketTextStream("localhost", 4444)
val events: DataStream[Click] = rawStream.flatMap(Click(_))

//assign the timestamp and watermark generator.
val eventsWithTs = events.assignTimestampsAndWatermarks(new WatermarkGenerator)
{% endhighlight %}
*Note: WatermarkGenerator is an implementation of AssignerWithPeriodicWatermarks, you can find the implementation [here](https://github.com/soniclavier/bigdata-notebook/blob/master/flink/src/main/scala/com/vishnu/flink/streaming/sessionwindows/SessionWindowExample.scala#L77).*

To calculate the number of clicks per user during a session, we need to key the stream based on userId. Once a stream is keyed, all the message with the same key will be part of the same Stream. Next, we have to define the Session gap (timeout). In this example, we will create an EventTime based SessionWindow with a gap of 2 seconds and also set an AllowedLateness of maximum 4 seconds. AllowedLateness allows messages that come delayed to be processed. Flink keeps the Window alive till it's MaxTimestamp + AllowedLateness.
{% highlight scala %}
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
{% endhighlight %}
### Execution and Under the hood
Let us now test the application by sending few messages. We will send Click messages from 2 users - User1 and User2. User1 will send a click event at 0th second, 1st second, 2nd second and 8th second. Where the click at 2nd second is delayed and arrives only at 5.2nd second. User2 will send just one click event at 4.5th second. 

<div class="col three">
    <img class="col three expandable" src="/img/flink_session/session_merge.png">
</div>

***Note*** : The reason I included a User2 is to show how Watermark is advanced by Flink and how that affects the results. The Watermark generator that we used in this application keeps track of the latest timestamp seen so far and uses it as the CurrentWatermark. So when the Click event from User2 arrives at 4.5th second, Flink will understand that the EventTime is now 4.5 second (some day, hour and minute). At this point the User1's Window with 2 Click events will be evaluated - producing output *(user1,2)*, since the Window's end point(max timestamp) is at 3rd second which is less than the current watermark (behavior of the default trigger for EventTimeSessionWindow - EventTimeTrigger). But this Window will be kept alive since Max Timestamp + Allowed Lateness is not less than the CurrentWatermark. So, when the late message arrives, it will be put into this Window and the Window will be evaluated again to produce output *(user1, 3)*. Note that in this case the Window was evaluated 2 times, this is something you will have to take care when using AllowedLateness. If you don't want this behavior, the only way is to make CurrentWatermark lag behind max timestamp by "maximum delay" that you expect the messages to arrive.(Check [this](flink_eventtime#watermarks) blog to see how it can be done). The drawback with the Watermark approach is that Window will be evaluated only after MaxTimestamp + MaxDealy, even if there are no late arriving messages. 

When using Session window, Flink initially puts each message in its own window. This Window's end time being timestamp + session gap. Then, it gets all the Windows for that key(userId) and merges them if there are any overlaps. 

This execution will produce output - (user1,2), (user1,3), (user2,1), (user1,1). I hope you understood how this output is produced, the first two entries were from the first 3 Clicks of User1 (two entries due to re-evaluation of window due to late message). Third entry(user2, 1) from the one Click from User2. The last entry (user1, 1) is from the last Click we received from User1. This Click belongs to a new Session since it was received only at 8th second, which is > MaxTime (4th second) of the previous Window.

Session windows are very useful since it aligns very well with the events that we receive from a Web application. The reason I wrote this blog is that someone asked me about Session Windows and I could not find much material online on how to use it along with EventTime. This was more of self-learn + share. Hope you liked it and thanks for reading.	
<br/><a href="search.html?query=flink">Continue reading</a>