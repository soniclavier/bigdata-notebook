---
layout: post
comments: true
title: Kafka Streams - Part 2
date: 2017-05-07
PAGE_IDENTIFIER: kafka-streams-part2
permalink: /kafka-streams-part2.html
image: /img/kafka_streams/header_share2.png
tags: Kafka BigData Hadoop Scala Streaming
description: This is continuation of the blog post - "Hello Kafka Streams". In this blog we  build a bit more complicated application that demonstrates the use of flatMapValues, branch, predicate, selectKey, through, join and also see how to create a custom SerDe using Kryo.
---
<div class="col three">
    <img class="col three" src="/img/kafka_streams_2/logo.png">
</div>
This is Part 2 of the blog on Kafka Streams, in the previous blog [Hello Kafka Streams](hello-kafka-streams), we built a simple stream processing application using Kafka Streams Library. In this blog, we will continue exploring more features in Kafka Streams by building a bit more involved application which explains the use of flatMapValues, branch, predicate, selectKey, through, join and also see how to create a custom SerDe using Kryo. All the code used in this blog can be found in my [Github](https://github.com/soniclavier/hadoop_datascience/blob/master/KafkaStreams/src/main/scala-2.11/com/vishnuviswanath/kafka/streams/ClimateLogStream.scala) <i class="fa fa-github" aria-hidden="true"></i>.

In this post, our input will be a stream of ClimateLog messages which will be of the format *<country, state, temperature, humidity>*. We will be creating a streaming application that has the below topology. 
<div class="col three">
    <img class="col three expandable" src="/img/kafka_streams_2/topology.png">
</div>
*climate_events* is a topic where we receive the ClimateLog messages in String format. These raw messages are parsed into *case class ClimateLog*.

{% highlight scala %}
case class ClimateLog(country: String, state: String, temperature: Float, humidity: Float)
val rawStream: KStream[String, String] = kstreamBuilder.stream(Serdes.String, Serdes.String, "climate_events")

val climateLogStream: KStream[String, ClimateLog] = rawStream.flatMapValues(new ValueMapper[String, Iterable[ClimateLog]]{
  override def apply(value: String): Iterable[ClimateLog] = ClimateLog(value).toIterable.asJava
})
{% endhighlight %}

<blockquote>Note that all messages in Kafka should have a Key and a Value. If we do not pass a key during ingestion through KafkaConsoleProducer, it will be null.</blockquote>

### **branch**
Branch creates multiple branches from a single stream. It takes in Varargs of Predicates and produces a KStream of each Predicate. Each element in the source KStream is applied against each Predicate and the element is assigned to the KStream corresponding to the first Predicate that it matches. In our example, we will create two predicates one for highHumidity and the other for lowTemp.

{% highlight scala %}
//define the predicates to split the stream into branches
val highHumidty = new Predicate[String, ClimateLog] {
  override def test(t: String, c: ClimateLog): Boolean = c.humidity > 50
}
val lowTemp = new Predicate[String, ClimateLog] {
  override def test(t: String, c: ClimateLog): Boolean = c.temperature < 0
}
//array of streams for each predicate
val branches = climateLogStream.branch(highHumidty, lowTemp)
{% endhighlight %}


### **through**
Through persists the messages from a KStream to the given topic and creates a new KStream from that topic. This can be used if you want the intermediate result from the application to be made available to other application, but at the same time use the stream further downstream in the current application. We will persist lowTemp stream and highHumidity stream to 2 new topics - low_temp and high_humidity. 

{% highlight scala %}
val highHumidityStream = branches(0).through(new Serdes.StringSerde, new ClimateLogSerDe, "high_humidity")
val lowTempStream = branches(1).through(new Serdes.StringSerde, new ClimateLogSerDe, "low_temp")
{% endhighlight %}

Note that the Value serializer is a custom Kryo based serializer for ClimateLog, which we will be creating next.

### **kryo serializer**
The serializer needs to implement `org.apache.kafka.common.serialization.Serde`. *Serde* has mainly two methods - serializer() and deserializer() which return instance of Serializer and Deserializer. Kafka expects this class to have an empty constructor. So, we will create a class ClimateLogSerDe which extends ClimatelogWrappedSerde class, which takes the Serializer and Deserializer as arguments in it's constructor. We also create ClimateLogSerializer and ClimateLogDeserializer which uses ClimateLogKryoSerDe as default serializer. The implementation is bit lengthy, please check the [github page](https://github.com/soniclavier/hadoop_datascience/blob/master/KafkaStreams/src/main/scala-2.11/com/vishnuviswanath/kafka/streams/ClimateLogStream.scala#L124-L194) for complete code.

### **selectKey**
The streams we have till now does not have a key (assuming you are using KafkaConsoleProducer and is not passing a key). *selectKey* selects a key using the KeyValueMapper provided and creates a new stream from the existing stream. We create two streams from highHumdityStream and lowTempStream by choosing *value.country* as the key.

{% highlight scala %}
val keyedHighHumStream: KStream[String, ClimateLog] = highHumidityStream.selectKey(new KeyValueMapper[String, ClimateLog, String] {
  override def apply(key: String, value: ClimateLog): String = value.country
})

val keyedLowTempStream: KStream[String, ClimateLog] = lowTempStream.selectKey(new KeyValueMapper[String, ClimateLog, String] {
  override def apply(key: String, value: ClimateLog): String = value.country
})
{% endhighlight %}

### **join**
Next, we join the highHumidity stream and lowTemperature stream to create a new stream called warnings. The two streams will be joined based on the key - which in this case is the country. We should also define a join window,
{% highlight scala %}
//create a join window. This window joins all the elements of the same key if the difference between their timestamps is within 60 seconds
val joinWindow = JoinWindows.of(60 * 1000)
{% endhighlight %}  
Now join the streams using a ValueJoiner. A ValueJoiner defines what should be done when we find two values for the same key. In this example, we simply merge these two values by getting the temperature from low temp stream and humidity from high humidity stream.
{% highlight scala %}
val warningsStream: KStream[String, String] = keyedHighHumStream.join[ClimateLog, String](
  keyedLowTempStream,
  new ValueJoiner[ClimateLog, ClimateLog, String] {
    override def apply(value1: ClimateLog, value2: ClimateLog): String = value2.copy(humidity = value1.humidity).toString
  },
  joinWindow)  
{% endhighlight %} 

Finally, we store the warningsStream to another topic called "warnings", and then start the stream.

{% highlight scala %}
warningsStream.to(new Serdes.StringSerde, new Serdes.StringSerde, "warnings")

val streams = new KafkaStreams(kstreamBuilder, settings)
streams.start 
{% endhighlight %} 

We have already seen how to submit the job, how to create the topics(climate_events, high_humidity, low_temp, warnings) and how to send the message to these topics in the previous [blog post](hello-kafka-streams#hello-kafka-streams), so I am not going to bore you with the same details :) 

To summarize, we saw how to use various KafkaStreams APIs such as - branch, through, selectKey, join. We also created a custom serializer using Kryo. Hope this was useful and Thanks for reading!
<br/><a href="search.html?query=kafka">Continue reading</a>