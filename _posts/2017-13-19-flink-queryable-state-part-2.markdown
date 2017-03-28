---
layout: post
comments: true
title: Queryable States in ApacheFlink - Implementation
date: 2017-03-25
PAGE_IDENTIFIER: flink_queryable_state_impl
permalink: /flink_queryable_state2.html
image: /img/flink_queryable_state/queryable_flow2.png
tags: ApacheFlink BigData Hadoop Scala Streaming
description: This is part 2 of the blog Queryable States in Apache Flink. In the previous blog we saw how Apache Flink enabled Queryable States. In this part we will create a Streaming Job with Queryable States and create a QueryClient to query the state.
---
<div class="col three">
    <img class="col three" src="/img/flink_queryable_state/header.png">
</div>

This is part 2 of the blog Queryable States in Apache Flink. In the previous [blog](flink_queryable_state1.html), we saw how Apache Flink enabled Queryable States. In this part, we will create a Streaming Job with Queryable States and create a QueryClient to query the state. I assume that Flink is already installed and setup. If not you can check out my earlier blog post on installation [here](flink_start.html). I will be using a Tumbling window in this example, to read about Windows in Flink, please read [this](flink_streaming.html) blog post.

All the code used in this blog post will be available on my [GitHub](https://github.com/soniclavier/hadoop_datascience/tree/master/flink/src/main/scala/com/vishnu/flink/streaming/queryablestate) <i class="fa fa-github" aria-hidden="true"></i>.

### **Creating the Pipeline**
Let us now create a streaming job with QueryableState. In this example, our input is climate log which is of the format `country, state, temperature, humidity` where country and state are Strings, temperature and humidity are Floats. We will first create case class to hold these logs.

{% highlight scala %}
case class ClimateLog(country: String, state: String, temperature: Float, humidity: Float)
  object ClimateLog {
    def apply(line: String): Option[ClimateLog] = {
      val parts = line.split(",")
      try{ Some(ClimateLog(parts(0), parts(1), parts(2).toFloat, parts(3).toFloat)) } 
      catch {
        case e: Exception => None } } }
{% endhighlight %}

We can then read the logs from a socket using

{% highlight scala %}
val climateLogStream = senv.socketTextStream("localhost", 2222)
      .flatMap(ClimateLog(_))
{% endhighlight %}

We will create a KeyedStream and apply a [Tumbling](flink_streaming.html) TimeWindow of 10 seconds. This will cause the window to be evaluated each time it tumbles.  In the apply function, we will do a simple aggregation to sum up all the values of temperatures and humidities seen in that window.

{% highlight scala %}
val climateLogAgg = climateLogStream
      .keyBy("country", "state")
      .timeWindow(Time.seconds(10))
      .apply((key: Tuple, w: TimeWindow, clogs: Iterable[ClimateLog], out: Collector[ClimateLog]) => {
        val agg = clogs.reduce((c1: ClimateLog, c2: ClimateLog) => c1.copy(
          temperature = c1.temperature + c2.temperature,
          humidity=c1.humidity + c2.humidity))
        out.collect(agg)
      })
{% endhighlight %}
#### **QueryableStateStream**
Now we will create a Stream that is queryable. To do that, we need a StateDescriptor that describes the type of elements that are going to be stored in the stream. We will create a ReducingStateDescriptor that aggregates the values seen so far. The ReducingStateDescriptor takes three parameters, first parameter is the name, second is the reducing function that has to be applied when new elements are added to the state, and the third describes the type of values that are going to be stored in the state.
{% highlight scala %}
val climateLogStateDesc = new ReducingStateDescriptor[ClimateLog](
  "climate-record-state",
  reduceFunction,
  TypeInformation.of(new TypeHint[ClimateLog]() {}))

val reduceFunction = new ReduceFunction[ClimateLog] {
  override def reduce(c1: ClimateLog, c2: ClimateLog): ClimateLog = {
    c1.copy(
      temperature = c1.temperature + c2.temperature,
      humidity=c1.humidity + c2.humidity) } }
{% endhighlight %}

Once that is done, we call `asQueryableState` function to make the stream queryable and pass the state descriptor created.This is shown below.
{% highlight scala %}
val queryableStream = climateLogAgg
  .keyBy("country")
  .asQueryableState("climatelog-stream", climateLogStateDesc)
senv.execute("Queryablestate example streaming job")
{% endhighlight %}
Note the first parameter while calling the `asQueryableState` state function, this is the `queryableStateName` which is used for identifying the stream. This will be later used by the QueryClient while querying.

#### **QueryClient**
Now we will move on to the creating the QueryClient. The client is going to be a separate application that queries the state of an already running Streaming job. First thing that the client needs to know is how to connect to the JobManager (remember the diagram from the previous blog?), which can be configured as follows

{% highlight scala %}
val config = new Configuration
config.setString(ConfigConstants.JOB_MANAGER_IPC_ADDRESS_KEY, "localhost")
config.setString(ConfigConstants.JOB_MANAGER_IPC_PORT_KEY, "6123")
{% endhighlight %}

Next we create an instance of QueryableStateClient and also Serializers for key and the value. The key serializer is used to create a serializedKey. The value serializer will be used later to deserialize the result returned back from the query. In the below example, we are asking the state to return the current running state value for the country "USA".

{% highlight scala %}
val client = new QueryableStateClient(config)
val execConfig = new ExecutionConfig
val keySerializer = createTypeInformation[String].createSerializer(execConfig)
val valueSerializer = TypeInformation.of(new TypeHint[ClimateLog]() {}).createSerializer(execConfig)
val key = "USA"
val serializedKey = KvStateRequestSerializer.serializeKeyAndNamespace(
  key,
  keySerializer,
  VoidNamespace.INSTANCE,
  VoidNamespaceSerializer.INSTANCE)
{% endhighlight %}

Now we can query the state using the client. Pass the serializedKey, JobID and queryableStateName as parameters. JobID can be obtained either from the Flink UI or from the job submission log. Note that `climatelog-stream` parameter which should be same as the queryableStateName used during job submission.
{% highlight scala %}
val serializedResult = client.getKvState(jobId, "climatelog-stream", key.hashCode(), serializedKey)
{% endhighlight %}
The query returns a Future object which can be accessed as follows. If the query was successful, then we can use the valueSerializer to deserialize and read the result. In this case, the deserialized result is an instance of the ClimateLog case class. 
{% highlight scala %}
serializedResult onSuccess {
  case result ⇒ {
    try {
      val clog: ClimateLog = KvStateRequestSerializer.deserializeValue(result, valueSerializer)
      println(s"State value: $clog")
    } catch {
      case e: Exception ⇒ e.printStackTrace() } } } 
serializedResult onFailure {
  case uk :UnknownKeyOrNamespace ⇒ println(uk.getMessage)
  case e: Exception ⇒ println(e.getMessage) }
{% endhighlight %}

To test the job, open a terminal and run netcat.
{% highlight console %}
nc -lk 2222
{% endhighlight %}
Now submit the job using flink command line interface
{% highlight console %}
flink run target/scala-2.11/flink-vishnu-assembly-1.0.jar
Submitting job with JobID: ec685d96da49644ab025c8f9a27ca07a. Waiting for job completion
{% endhighlight %}
Now all that is left to do is send some sample messages through netcat, and run the QueryClient with the JobId and other parameters.

There are a few possible Exceptions that can occur at this point. 

1) Actor not found
{% highlight java %}
Actor not found for: ActorSelection[Anchor(akka.tcp://flink@localhost:6123/), Path(/user/jobmanager)]
{% endhighlight %} 
Make sure that your Flink cluster is up and running. Also you have to submit the Job through the command line, not from the IDE. 

2) Job not found
{% highlight java %}
java.lang.IllegalStateException: Job d8a3b9f9b8e6da33aa714633cee61c3b not found
{% endhighlight %} 
This is an easy one, just make sure that JobId passed matches with that of the running job.

3) No KvStateLocation found
{% highlight java %}
org.apache.flink.runtime.query.UnknownKvStateLocation: No KvStateLocation found for KvState instance with name 'climatelog-stream-temp'
{% endhighlight %} 
Make sure that the state name(climatelog-stream) in the client matches with the one that was used during job submission.

4) KvState does not hold any state for key/namespace
{% highlight java %}
org.apache.flink.runtime.query.netty.UnknownKeyOrNamespace: KvState does not hold any state for key/namespace
{% endhighlight %} 
This means that the stream that you are tying to query does not have the key(in this example - "USA") that you are looking for. Did the messages that were sent through netcat have the key that is being used in the query?

5) Could not deserialize value
{% highlight java %}
java.io.EOFException
  at org.apache.flink.runtime.util.DataInputDeserializer.readUnsignedByte(DataInputDeserializer.java:310)
  at org.apache.flink.types.StringValue.readString(StringValue.java:770)
  at org.apache.flink.api.common.typeutils.base.StringSerializer.deserialize
{% endhighlight %} 
Which indicates that something is wrong with the ValueSerializer. The easiest way to fix this is by going back to your Streaming Job code and making sure that you use the exact same TypeInformation in the client as used in the Job. e.g., using *createTypeInformation[ClimateLog]* instead of `TypeInformation.of(new TypeHint[ClimateLog]() {})` can cause exception.


To summarize, we saw how Apache Flink enables querying it's internal state and how we can develop a pipeline and query client to do so. Apart from Flink, Kafka also provides this feature.

That concludes the post and hope it was useful. Thanks for reading!
<br/><a href="search.html?query=flink">Continue reading</a>