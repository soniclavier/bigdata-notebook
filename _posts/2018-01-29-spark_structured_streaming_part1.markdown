---
layout: post
comments: true
title: A Tour of Spark Structured Streaming
date: 2018-01-29
PAGE_IDENTIFIER: spark_structured_streaming
permalink: /spark_structured_streaming.html
image: /img/spark_structured_streaming/header_share.png
show_index: true
tags: ApacheSpark Kafka Streaming Scala BigData Hadoop
description: Structured Streaming is Apache Spark's streaming engine which can be used for doing near real-time analytics. In this blog we explore Structured Streaming by going through a very simple use case.
---
<div class="col three">
	<img class="col three" src="/img/spark_structured_streaming/header.png">
</div>
Structured Streaming is Apache Spark's streaming engine which can be used for doing near real-time analytics. In this blog, we explore Structured Streaming by going through a very simple use case. Imagine you started a ride hauling company and need to check if the vehicles are over-speeding. We will create a simple near real-time streaming application to calculate the average speed of vehicles every few seconds. All the code used in this blog is available in my [Github repository](https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala). <i class="fa fa-github" aria-hidden="true"></i>

### **Micro Batch based Streaming**
Before we jump into the use case, let us take a look at how streaming works under the hood in Apache Spark. Structured Streaming in Spark, similar to its predecessor (DStream) uses **micro-batching** to do streaming. That is, spark waits for a very small interval say 1 second (or even 0 seconds - i.e., as soon as possible) and batches together all the events that were received during that interval into a micro batch. This micro batch is then scheduled by the Driver to be executed as Tasks at the Executors. After a micro-batch execution is complete, the next batch is collected and scheduled again. This scheduling is done frequently to give an impression of streaming execution.

<div class="col three">
	<img class="col three expandable" src="/img/spark_structured_streaming/microbatching.png">
</div>

### **Kafka Source**
We will be reading the events from a Kafka topic - *cars*. To do that, we need to set the **format** as "kafka", set **kafka.bootstrap.server** with the broker address and provide the topic name using the option "subscribe".
{% highlight scala %}
val df: DataFrame = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "cars")
      //.schema(schema)  : we cannot set a schema for kafka source. Kafka source has a fixed schema of (key, value)
      .load()
{% endhighlight %}
To simulate a vehicle sending us sensor data, we will create a Kafka producer that writes id, speed, acceleration and the timestamp to the "cars" topic. Code for which can be found here [RandomCarsKafkaProducer.scala](https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/src/main/scala/com/vishnuviswanath/spark/util/RandomCarsKafkaProducer.scala). Note that the timestamp here is called the **EventTime**, because it is the time at which the event(message) was generated at its source.

<div class="col three">
	<img class="col three expandable" src="/img/spark_structured_streaming/car_event.png">
</div>
*Note: if you need to setup local Kafka broker, instructions are available [here](https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L14-L32).*

Next, we parse the raw data into a case class so that we have a structure to work with.

{% highlight scala %}
case class CarEvent(carId: String, speed: Option[Int], acceleration: Option[Double], timestamp: Timestamp)

object CarEvent {
	def apply(rawStr: String): CarEvent = {
	  val parts = rawStr.split(",")
	  CarEvent(parts(0), Some(Integer.parseInt(parts(1))), Some(java.lang.Double.parseDouble(parts(2))), new Timestamp(parts(3).toLong))
	}
}
val cars: Dataset[CarEvent] = df
  .selectExpr("CAST(value AS STRING)")
  .map(r ⇒ CarEvent(r.getString(0)))
{% endhighlight %}
This produces a DataSet of type CarEvent.
### **Performing Aggregation**
We start off simple by finding the average speed of each vehicle. This can be done by doing a **groupby** on carId and by applying the **avg** aggregate function.
{% highlight scala %}
val aggregates = cars
    .groupBy("carId")
    .agg(
      "speed" → "avg"
    )

{% endhighlight %}
This calculates the average speed of events received during every micro-batch. In Structured Streaming, the micro-batch interval can be controlled using **Triggers**. *Spark’s idea of Trigger is slightly different from event-at-a-time streaming processing systems such as [**Flink**](search.html?query=flink) or **Apex**. In Spark, a trigger is set to specify how long to wait before checking if new data is available. If no trigger is set, Spark will check for availability of new data as soon as the previous micro-batch execution is complete. Whereas in event-at-a-time systems, as the new data comes in, it is collected in the window’s internal state until the trigger fires.*

That was easy! But what if we want to calculate the average speed of a vehicle over last 5 seconds. Also, we would like to calculate it based on the **EventTime** of the events (i.e., based on the time at which the event occurred at the source, not based on when it was processed in the system.) If you don't know what EventTime is, read on.

### **EventTime & ProcessingTime**
**EventTime** is the time at which an event is generated at its source, whereas a **ProcessingTime** is the time at which that event is processed by the system. There is also one more time which some stream processing systems account for, that is **IngestionTime** - the time at which event/message was ingested into the System. It is important to understand the difference between EventTime and ProcessingTime.
<div class="col three">
	<img class="col three expandable" src="/img/spark_structured_streaming/car_eventtime.png">
</div>
The red dot in the above image is the message, which originates from the vehicle, then flows through the Kafka topic to Spark's Kafka source and then reaches executor during task execution. There could be a slight delay (or maybe a long delay if there is any network connectivity issue) between these points. The time at the source is what is called an **EventTime**, the time at the executor is what is called the **ProcessingTime**. You can think of the ingestion time as the time at when it was first read into the system at the Kafka source (IngestionTime is not relevant for spark).

Now that you have a fair idea of different time characteristics, let us get back to the use-case of calculating the average speed of cars over last 5 seconds. To do that we need to group the events into 5-second interval time groups, based on its EventTime. This grouping is called Windowing.

### **Windows**
In Spark, Windowing is done by adding an additional key (window) in the groupBy clause. For each message, its EventTime(timestamp generated by the sensor) is used to identify which window the message belongs to. Based on the type of window (Tumbling/Sliding) an event might belong to one or more windows. To understand how, we need to first learn what a TumblingWindow and a SlidingWindow are.

#### **Tumbling Window & Sliding Window**
A tumbling window is a non-overlapping window, that tumbles over every "window-size". e.g., for a Tumbling window of size 4 seconds, there could be window for [00:00 to 00:04), [00:04: 00:08), [00:08: 00:12) etc (ignoring day, hour etc here). If an incoming event has EventTime 00:05, that event will be assigned the window - [00:04 to 00:08)

A SlidingWindow is a window of a given size(say 4 seconds) that slides every given interval (say 2 seconds). That means a sliding window could overlap. For a window of size 4 seconds, that slides every 2 seconds there could windows [00:00 to 00:04), [00:02 to 00:06), [00:04 to 00:08) etc. Notice that the windows 1 and 2 are overlapping here. If an event with EventTime 00:05 comes in, that event will belong to the windows [00:02 to 00:06) and [00:04 to 00:08).

<div class="col three">
	<img class="col three expandable" src="/img/spark_structured_streaming/tumbling_sliding.png">
</div>

To do windowing, Spark adds a new column called "window" and explodes the provided 'timestamp' column into one or more rows(based on its value and the window's size and slide) and do a groupby on that column. This implicitly pulls all the events that belong to a time-interval into same "window". 

*Side note: A tumbling window can also be thought of as a sliding window whose slide interval is same as the window size. i.e., a sliding window of size 4 seconds that slides every 4 seconds is same as a tumbling window of size 4 seconds. In fact, that is exactly what Spark does internally.*

Here we group the cars DataSet based on 'window' and carId. *Note that `window()` is a function in Spark that returns a Column.*
{% highlight scala %}
//a tumbling window of size 4 seconds
val aggregates = cars
	.groupBy(window($"timestamp","4 seconds"), $"carId")
	.agg(avg("speed").alias("speed"))
	.where("speed > 70")

//a sliding window of size 4 seconds that slides every 2 seconds can be created using cars.groupBy(window($"timestamp","4 seconds","2 seconds"), $"carId")
{% endhighlight %}

This produces a DataFrame of carId, avg speed, and the corresponding time window. e.g output: 

- Batch 1
	- [2018-01-21 00:50:00, 2018-01-21 00:50:04]	car1  75.0      

### **Output Modes**
The final(almost) piece of the puzzle is to output the results that we produced to a sink - a **Kafka topic**. Spark provides three output modes - **Complete, Update and Append**. Each mode differs in how Spark updates the state and outputs the results after processing a micro-batch. 
<div class="col three">
	<img class="col three expandable" src="/img/spark_structured_streaming/output_modes.png">
</div>
During each micro-batch, Spark updates values for some of the keys from the previous batch, some are new and some remains the same. In the Complete mode, all the rows are output, in Update mode only the new and updated rows are output. Append mode is slightly different in that, in Append mode, there won't be any updated rows and it outputs only the new rows.

### **Kafka Sink**
Writing to Kafka is pretty straightforward -  set format as "kafka", point the sink to the Kafka broker using option **kafka.bootstrap.server**, and set the option **topic** to tell which Kafka topic to write to. Kafka sink expects a field - **value** to be present in the data. We can make use of Spark SQL's **selectExpr** to convert the field *'speed'* to *'value'* and also cast it to String. The **key** is optional but if you have multiple partitions and wants to distribute the data across partitions, it is needed. A **checkpointLocation** is a must when using Kafka sink and it enables failure recovery and exactly once processing.

{% highlight scala %}
val writeToKafka = aggregates
  .selectExpr("CAST(carId AS STRING) AS key", "CAST(speed AS STRING) AS value")
  .writeStream
  .format("kafka")
  .option("kafka.bootstrap.servers","localhost:9092")
  .option("topic", "fastcars")
  .option("checkpointLocation", "/tmp/sparkcheckpoint/")
  .queryName("kafka spark streaming kafka")
  .outputMode("update")
  .start()
{% endhighlight %}

Output of running the application will look something like this:
- Batch: 1
	- [2018-01-21 00:50:00, 2018-01-21 00:50:04]	car1  	75.0
- Batch: 2
	- [2018-01-21 00:50:04, 2018-01-21 00:50:08]	car2  	20.0
	- [2018-01-21 00:50:12, 2018-01-21 00:50:16]	car2  	20.0     
	- [2018-01-21 00:50:00, 2018-01-21 00:50:04]	car1  	62.5 

Note that Structured Streaming API implicitly maintains the **state** across batches for aggregate functions, i.e., in the above example, the average speed calculated in the second micro-batch will be average of events received during the 1st and 2nd batch. So as a user you don't have to do custom state management. But that comes with the cost of maintaining a large state over time, and no one want to keep the state forever. This can be achieved using watermarks.

### **Watermark**
In Spark, Watermark is used to decide when to clear a state based on current maximum event time. Based on the **delay** you specify, Watermark lags behind the maximum event time seen so far. e.g., if dealy is 3 seconds and current max event time is 10:00:45 then the watermark is at 10:00:42. This means that Spark will keep the state of windows who's end time is less than 10:00:42.

{% highlight scala %}
val aggregates = cars
  .withWatermark("timestamp", "3 seconds") //set watermark using timestamp filed with a max delay of 3s.
  .groupBy(window($"timestamp","4 seconds"), $"carId")
  .agg(avg("speed").alias("speed"))
  .where("speed > 70")
{% endhighlight %}

A subtle but important detail to understand is that when using EventTime based processing, time progresses only if you receive a message/event with a higher timestamp value. Think of it as clock inside Spark, but unlike normal clocks that ticks every second(ProcessingTime based) this clock only moves when you receive an event with a higher timestamp.

Let us look at an example to see how this works when there is a late arriving message. We will focus on a single window between [10:00 to 10:10) and a maximum delay of 5 seconds. i.e., `.withWatermark("timestamp", "5 seconds")`

<div class="col three">
	<img class="col three expandable" src="/img/spark_structured_streaming/watermark_eg.png"/>
</div>


- An event with timestamp 10:00 arrives, falls in the window [10:00, 10:10) and watermark is updated as timestamp - 5
- An event with timestamp 10:02 is generated at the source, but is delayed. This event is supposed to fall in window [10:00, 10:10)
- An event with timestamp 10:04 arrives late at 10:05, but this still falls in the window [10:00, 10:10) since the current watermark is 09:55 which is < window end time. Watermark is updated to 10:04 - 00:05 = 09:59.
- An event with timestamp 10:16 arrives, this updates watermark to 10:11. (This event will fall into window [10:10, 10:20), but is not relevant here).
- Late event with timestamp 10:02 arrives, but the window [10:00, 10:10) is cleared, so this event will be dropped.

Setting watermark will ensure that state does not grow forever. Also, notice how one of the late events was processed while the other was ignored (since it was too late).

### **Conclusion**
We have built a simple streaming application while explaining EventTime processing, Windowing, Watermarks, Output modes and how to read and write to Kafka. The code for this and some more examples are available in my [Github repository](https://github.com/soniclavier/bigdata-notebook/tree/master/spark_23). I hope this gave a better insight on some of the new features in Spark Structured Streaming. Let me know if there is any question in the comments. Thanks for reading! 

<a href="search.html?query=spark">Continue reading</a>
