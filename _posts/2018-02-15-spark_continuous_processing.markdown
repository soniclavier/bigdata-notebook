---
layout: post
comments: true
title: Spark Continuous Processing
date: 2018-02-25
PAGE_IDENTIFIER: spark_structured_streaming
permalink: /spark_streaming_continuous_processing.html
image: /img/spark_continuous/header_share.png
show_index: false
tags: ApacheSpark Kafka Streaming Scala BigData Hadoop
description: Continuous Processing is Apache Spark's new Execution engine that allows very low latency(in milliseconds) event at a time processing. In earlier versions, streaming was done via micro-batching. In continuous processing, Spark launches long-running tasks that continuously read, process and write data. In this blog, we are going to do an early peek at this still experimental feature in Apache Spark that is going to be available in version 2.3.
---
<div class="col three">
	<img class="col three" src="/img/spark_continuous/header.png">
</div>
Continuous Processing is Apache Spark's new Execution engine that allows very low latency(in milliseconds) event at a time processing. In this blog we are going to do an early peek at this still experimental feature in Apache Spark that is going to be available in version 2.3. I am going to assume that you are already familiar with Spark's micro-batch based execution engine. If you are not, do read my previous blog post [here](spark_structured_streaming.html). The code used in this blog post is available in my [Github repo](https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/src/main/scala/com/vishnuviswanath/spark/streaming/ContinuousKafkaStreaming.scala) <i class="fa fa-github" aria-hidden="true"></i> 

### **From MicroBatch to ContinuousProcessing**
Apache Spark has been providing stream processing capabilities via micro-batching all this while, the main disadvantage of this approach is that each task/micro-batch has to be collected and scheduled at regular intervals, through which the best(minimum) latency that Spark could provide is around 1 second. There was no concept of a single event/message processing. Continuous processing is Spark's attempt to overcome this limitations to provide stream processing with very low latencies.

To enable this features, Spark had to make two major changes in its underlying code. 

  1. Create new sources and sinks that could read message continuously(instead of micro-batch) - called DataSourceV2.
  2. Create a new execution engine called - ContinuousProcessing which uses ContinuousTrigger and launch long runnings tasks using DataSourceV2.

<div class="col three">
  <img class="col three expandable" src="/img/spark_continuous/long_running.png">
</div>

### **DataSourceV2**
DataSourceV2 has the ability read/write record at a time. For example, the KafkaSource has *get()* and *next()* methods to read each record, instead of the *getBatch()* method in V1. *(Note: even though the records are read one at a time, there is still some buffering done at the KafkaConsumer)*

KafkaSink runs continuously waiting for new records to be committed to the topic and writes/commits record at a time. 

#### **Available Sources**
Readers supported right now are 
  - KafkaSource(short name *kafka*)
  - RateSource(short name *rate*) - for testing purpose only

Writers supported right now are
  - KafkaSink 
  - ConsoleSink - for testing purpose only
  - MemorySink - for testing purpose only

#### **Custom Source/Sink**
It is not very difficult to create your own reader/writer. I have an example of a source - [NetcatContinuousReader](https://github.com/soniclavier/bigdata-notebook/tree/master/spark_23/src/main/scala/com/vishnuviswanath/spark/streaming/sources/netcat) and an [application](https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/src/main/scala/com/vishnuviswanath/spark/streaming/CustomV2SourceExample.scala) that uses this source in my Github. 

### **ContinuousExecution Engine**
This is the second major change that allows low latency processing in Spark. A ContinuousExeuction engine is chosen as the StreamExecution when the trigger set is ContinuousTrigger (also the source and sink should be of the type DataSourceV2). The operations supported by this engine are limited for now, it supports mainly Map, Filter, and Project. Aggregation operations, joins, [windowing](spark_structured_streaming.html#windows) etc are not supported. The idea behind this is that for such operations we need to wait for sometime to collect the data, and in those use cases, the Micro-Batch based engine should suffice. The use cases that require very low latency(in milliseconds) are the ones that fit this model.

### **Example**
If you are already familiar with Spark's Structured Streaming API, the only change that needs to be made is in the Trigger - set the trigger as **ContinuousTrigger**. I will be trying to convert the [code](https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala) written as part of my previous [blog](spark_structured_streaming.html) to use ContinuousProcessing. As a first step I will set the trigger as ContinuousTrigger, rest of the code will remain same. 

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
    .trigger(Trigger.Continuous("10 seconds")) //10 seconds is the checkpoint interval.
    .start()
{% endhighlight %} 

This caused an exception, *org.apache.spark.sql.AnalysisException* - **Continuous processing does not support EventTimeWatermark operations**. Watermarks are not supported in ContinuousProcessing since that involves collecting data. So we will remove **withWatermark("timestamp", "3 seconds")** from the code.<br/>

Now the application threw another exception **Continuous processing does not support Aggregate operations**. As I mentioned earlier, Spark expects you to use micro-batch based processing if you need to do aggregations, since this involves waiting for data to arrive. Removing the code related to avg, groupBy and window fixes the problem and the application runs.

Note: ContinuousTrigger internally uses a ProcessingTimeExecutor(same as ProcessingTime trigger). But this does not have any effect on how often the data is processed since the tasks are already launched and is continuously processing the data.

### **Conclusion**
ContinuousExecution provides us ability to do very low latency processing, but is limited in what we can we can do. This can change in near future since this is a new feature and is in the experimental stage. Hope you liked the post and as always thanks for reading. 

<a href="search.html?query=spark">Continue reading</a>
