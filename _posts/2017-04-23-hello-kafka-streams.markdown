---
layout: post
comments: true
title: Hello Kafka Streams
date: 2017-04-23
PAGE_IDENTIFIER: hello-kafka-streams
permalink: /hello-kafka-streams.html
image: /img/kafka_streams/header.png
tags: Kafka BigData Hadoop Scala Streaming
description: Kafka Streams is a stream processing library on top of Apache Kafka. In this blog we will have a quick look at the basic concepts Kafka Streams and then build a simple Hello Streams application that reads messages (names of people) from a topic and writes “hello name” to another topic
---
<div class="col three">
    <img class="col three" src="/img/kafka_streams/header.png">
</div>
Kafka Streams is a stream processing library on top of Apache Kafka. Even though Kafka Streams might look very similar to [Apache Flink](search.html?query=flink), they are meant for different use cases. The main difference being that Flink is a cluster based analytics framework whereas Kafka Streams is a library that can be used to build applications that process messages from Kafka topics. Kafka Streams is tightly integrated with Kafka as its source which is a design choice, whereas Flink is more general purpose. The advantage of Kafka Streams is that it is light weight and it comes out of the box with Kafka (which is almost always the choice of messaging system in Big Data applications) therefore making it easy to build stream processing applications.

In this blog, we will have a quick look at the basic concepts Kafka Streams and then build a simple Hello Streams application that reads messages *(names of people)* from a topic and writes "hello *name*" to another topic. All the code used in this blog can be found in my [Github](https://github.com/soniclavier/hadoop_datascience/tree/master/KafkaStreams) <i class="fa fa-github" aria-hidden="true"></i>.

#### **Topology**
Similar to other stream processing systems, the topology in KafkaStreams defines from where to read the data from, how to process and where to save the results. It has mainly three types of nodes - **Source, Processor and Sink**, connected by edges called **Streams**.
<div class="col three">
    <img class="col three" src="/img/kafka_streams/topology.png">
</div>
#### **KStreams and KTables**
KStreams and KTables are main two abstractions that represent a Stream of messages - which are (key, value) pairs. A KTable can be thought of as a KStream with only the latest value for each key, and a KStream can be thought of as a stream of changes(changelogs) that happen to a KTable.
<div class="col three">
    <img class="col three" src="/img/kafka_streams/kstream_ktable.png">
</div>

### **Hello Kafka Streams**
Before we start writing the code, there are a few very easy environment setup steps to be done, which are - start Zookeeper, Kafka Broker and create the Topics. Run the below commands from the base folder of Kafka, which in my case is **~/kafka_2.11-0.10.2.0**.

{% highlight sh %}
kafka_2.11-0.10.2.0$ bin/zookeeper-server-start.sh config/zookeeper.properties
kafka_2.11-0.10.2.0$ bin/kafka-server-start.sh config/server.properties
{% endhighlight %}
This starts the Zookeeper at port 2181 and Kafka Broker at port 9092 (which are the defaults and can be changed by editing the config files). Next, we will create the topics needed for the application. 
{% highlight sh %}
kafka_2.11-0.10.2.0$ bin/kafka-topics.sh --create --topic names --replication-factor 1 --partitions 1 --zookeeper localhost:2181
kafka_2.11-0.10.2.0$ bin/kafka-topics.sh --create --topic hellostream --replication-factor 1 --partitions 1 --zookeeper localhost:2181
{% endhighlight %}

#### **Building the Application**
Create a new SBT project in your IDE and edit the build.sbt file as per [this](https://github.com/soniclavier/hadoop_datascience/blob/master/KafkaStreams/build.sbt) (*You can ignore the kryo dependencies for now*). Next, create an object called [HelloKafkaStreams.scala](https://github.com/soniclavier/hadoop_datascience/blob/master/KafkaStreams/src/main/scala-2.11/com/vishnuviswanath/kafka/streams/HelloKafkaStreams.scala), and create a Properties object with following properties - Kafka Broker Url , Key SerDe(Serializer and Deserializer) and value SerDe.
{% highlight scala %}
val settings = new Properties
settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "hello-kafka-streams")
settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
settings.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.serdeFrom(classOf[String]).getClass.getName)
settings.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.serdeFrom(classOf[String]).getClass.getName)
{% endhighlight %}

We will now create a stream builder and use it to create a KStream that reads from topic - names.
{% highlight scala %}
val kstreamBuilder = new KStreamBuilder
val rawStream: KStream[String, String] = kstreamBuilder.stream("names")
{% endhighlight %}

Now we map each value in the raw stream by using the method mapValues. mapValues takes an instance of a ValueMapper class, which will append the word "hello" to each name read from the names topic.
{% highlight scala %}
val helloStream: KStream[String, String] = rawStream.mapValues(new ValueMapper[String, String]{
  override def apply(value: String): String = s"hello $value"
})
{% endhighlight %}

Finally, we will write the result back to another topic and start the processing. The first two parameters in the "to" method are optional, if not provided Kafka will take the default serializers from the Properties object set initially.
{% highlight scala %}
helloStream.to(Serdes.String, Serdes.String, "hellostream")
val streams = new KafkaStreams(kstreamBuilder, settings)
streams.start
{% endhighlight %}
We can now build the application using "sbt assembly", and run the jar using the following command.
{% highlight sh %}
java -cp target/scala-2.11/KafkaStreams-assembly-1.0.jar com.vishnuviswanath.kafka.streams.HelloKafkaStreams
{% endhighlight %}
Now, open a terminal and start a kafka-console-producer to send some names to the "names" topic and open another terminal and start a kafka-console-consumer to listen to "hellostream" topic.
{% highlight sh %}
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic names
vishnu
bin/kafka-console-consumer.sh --topic hellostream --bootstrap-server localhost:9092 --from-beginning
hello vishnu
{% endhighlight %}
As you can see, it is very easy to build a simple stream processing application using Kafka Streams. In the next blog, we will build a bit more complicated application that demonstrates the use of flatMapValues, branch, predicate, selectKey, through, join and also see how to create a custom SerDe using Kryo.

Thanks for reading!
<br/><a href="search.html?query=kafka">Continue reading</a>