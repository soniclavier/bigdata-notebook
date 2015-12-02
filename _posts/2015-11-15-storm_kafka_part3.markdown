---
layout: post
comments: true
title: Realtime Processing using Storm-Kafka - Part3
date: 2015-11-05
PAGE_IDENTIFIER: spark_scala
permalink: /realtime-storm-kafka3.html
description: This is the final part of the series. Here we are dealing with developing the Kafka Spout, Storm Topology and execution of the project.
---
<div class="col three">
	<img class="col three" src="/img/storm_blog_header.png">
</div>

This is the last part of the blog *Realtime Processing using Storm and Kafka*. You can find the previous parts here - <a href="http://vishnuviswanath.com/2015/11/05/storm_kafka_part1.html">Part 1</a>, <a href="http://vishnuviswanath.com/2015/11/05/storm_kafka_part2.html">Part 2</a>. In this section we will develop the Kafka Spout, Storm Topology and execute the project.

The source code for this project is available in my <a href="https://github.com/soniclavier/hadoop/tree/master/stormkafka" target="blank">github</a>

#### **Creating Kafka Spout**
Kafka spout reads from the kafka topic we created. So it has to know how to connect to Kafka broker, the name of the topic from which it has to read, zookeeper root and consumer group id. Zookeeper root and group id is used by the spout to store the offset information of till where it has read from the topic. In case of failure, the spout can use this information to start reading from where it failed. If zkRoot is 'kafka' and consumer group id is 'sample_group', then /kafka/sample_group will be created in zookeeper.

{% highlight sh %}
[zk: localhost:2181(CONNECTED) 0] ls /
[controller_epoch, brokers, storm, zookeeper, kafka, admin, consumers, config]
[zk: localhost:2181(CONNECTED) 1] ls /kafka
[sample_group]
[zk: localhost:2181(CONNECTED) 2] ls /kafka/sample_group
[partition_0]
{% endhighlight %}

Below java method creates a KafkaSpout. It first creates SpoutConfig using the values form the default_config.properties file and then passes it on to KafkaSpout class. This method is written inside the class <a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/spout/SpoutBuilder.java">SpoutBuilder.java</a>
{% highlight java %}
public KafkaSpout buildKafkaSpout() {
	BrokerHosts hosts = new ZkHosts(configs.getProperty(Keys.KAFKA_ZOOKEEPER));
	String topic = configs.getProperty(Keys.KAFKA_TOPIC);
	String zkRoot = configs.getProperty(Keys.KAFKA_ZKROOT);
	String groupId = configs.getProperty(Keys.KAFKA_CONSUMERGROUP);
	SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, groupId);
	spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
	KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
	return kafkaSpout;
}
{% endhighlight %}

<br/>


#### **Building the Topology**
<a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/Topology.java">Topology.java</a> is the main class which connects all the spouts and bolts together. Below diagram shows how the spout and bolts are connected together. Kafka spout picks up message from the topic.SinkTypeBolt listens to the KafkaSpout. SinkTypeBolt emits the tuples in three streams. SOLR bolt listens to the solr stream of SinkTypeBolt and similarly HDFS bolt and MongoDB bolt listens to hdfs stream and the mongodb stream of the SinkTypeBolt respectively.

The Topology class uses SpoutBuilder and BoltBuilder to build all the spouts and bolts
{% highlight java %}
TopologyBuilder builder = new TopologyBuilder();	
KafkaSpout kafkaSpout = spoutBuilder.buildKafkaSpout();
SinkTypeBolt sinkTypeBolt = boltBuilder.buildSinkTypeBolt();
SolrBolt solrBolt = boltBuilder.buildSolrBolt();
HdfsBolt hdfsBolt = boltBuilder.buildHdfsBolt();
MongodbBolt mongoBolt = boltBuilder.buildMongodbBolt();
{% endhighlight %}

These spouts and bolts are linked together by the TopologyBuilder class. Each spout should define from which stream it should receive it's input from. e.g., If bolt 'B' wants to receive it's input from bolt 'A', then we should call
{% highlight java %}
builder.setBolt('B',boltBobj,1).shuffleGrouping("A");
{% endhighlight %}
If bolt 'A' is emitting multiple streams -x and y, then bolt 'B' should also specify the stream name of bolt 'A'. It would look something like 
{% highlight java %}
builder.setBolt('B',bolt,1).shuffleGrouping("A","x");`
{% endhighlight %}

{% highlight java %}
//set the kafkaSpout to topology
builder.setSpout(configs.getProperty(Keys.KAFKA_SPOUT_ID), kafkaSpout, kafkaSpoutCount);
//set the sinktype bolt
builder.setBolt(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),sinkTypeBolt,sinkBoltCount).shuffleGrouping(configs.getProperty(Keys.KAFKA_SPOUT_ID));
//set the solr bolt
builder.setBolt(configs.getProperty(Keys.SOLR_BOLT_ID), solrBolt,solrBoltCount).shuffleGrouping(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),SOLR_STREAM);
//set the hdfs bolt
builder.setBolt(configs.getProperty(Keys.HDFS_BOLT_ID),hdfsBolt,hdfsBoltCount).shuffleGrouping(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),HDFS_STREAM);
//set the mongodb bolt
builder.setBolt(configs.getProperty(Keys.MONGO_BOLT_ID),mongoBolt,mongoBoltCount).shuffleGrouping(configs.getProperty(Keys.SINK_TYPE_BOLT_ID),MONGODB_STREAM);
{% endhighlight %}

kafkaSpoutCount : parallelism-hint for the kafkaSpout - defines number of executors/threads to be spawn per container
<blockquote>Note: shuffleGrouping is one of the eight stream grouping methods available in Storm (it sends the tuples to bolts in random). Another type of grouping is fieldsGrouping - in fields grouping, the tuples are grouped based on a specified field and the tuples having same value for that field is always sent to the same task. We can also implement custom grouping by implementing the interface CustomStreamGrouping.
</blockquote>
Finally the topology can be submitted by
{% highlight java %}
Config conf = new Config();
conf.put("solr.zookeeper.hosts",configs.getProperty(Keys.SOLR_ZOOKEEPER_HOSTS));
String topologyName = configs.getProperty(Keys.TOPOLOGY_NAME);
//Defines how many worker processes have to be created for the topology in the cluster.
conf.setNumWorkers(1);
StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
{% endhighlight %}


#### **Execution**
For execution, we need to start the below servers

1. Hadoop servers
2. Solr server
3. Kafka broker
4. Mongod server
5. Storm nimbus
6. Storm supervisor
7. Storm UI (optional)

Build the jar using the command `mvn clean install1`. The command will create your toplogy jar with all the dependencies - `stormkafka-0.0.1-SNAPSHOT.jar`.
Run the jar using the command
{% highlight sh %}
$storm jar stormkafka-0.0.1-SNAPSHOT.jar com.vishnu.storm.Topology
...
768  [main] INFO  b.s.StormSubmitter - Successfully uploaded topology jar to assigned location: /Users/vishnu/apache-storm-0.10.0/storm-local/nimbus/inbox/stormjar-be5f5f13-c6d6-456d-b45e-2e7bbf6ba4c8.jar
768  [main] INFO  b.s.StormSubmitter - Submitting topology storm-kafka-topology in distributed mode with conf {"storm.zookeeper.topology.auth.scheme":"digest","storm.zookeeper.topology.auth.payload":"-8123923076974561721:-8924677632676109956","topology.workers":1,"solr.zookeeper.hosts":"localhost:2181"}
861  [main] INFO  b.s.StormSubmitter - Finished submitting topology: storm-kafka-topology
{% endhighlight %}
where `com.vishnu.storm` is the package name and `Topology` is the class containing the main method.
Open your storm UI at <a href="http://localhost:8080/">http://localhost:8080/</a> and verify that job has been deployed correctly. Storm UI provides a very good visualization of the toplogy, you can view it by clicking `your-tolology-name>Show Visualization`.

<div class="col three">
	<img class="col three" src="/img/storm_deployed.png"/>
</div>

Now let us insert some sample messages for each of the sinks - MongoDB, SOLR and HDFS and check if those messages makes their way to the destination.
To do that, start your kafka-console-producer. If you had forgotten the name of the kafka topic we created earlier (I know I did !) you can use the following command from kafka base folder.
{% highlight sh %}
bin/kafka-topics.sh --zookeeper localhost:2181 --list
//start the console producer
$bin/kafka-console-producer.sh --topic incoming --broker localhost:9092
//insert message
hdfs this message goes to hdfs
mongo id:1 value:mongodb_message
solr id:1 value:solr_message
{% endhighlight %}

We now verify each of the sinks

1) MongoDB - from your mongodb folder, you can run
{% highlight sh %}
$bin/mongo
use storm
db.collection1.find()
{ "_id" : ObjectId("56442855a9ee7800956aaf50"), "id" : "1", "value" : "mongodb_message" }
{% endhighlight %}
2) SOLR - You can see the Solr message by accessing the SOLR UI <a href="http://localhost:8983/solr/#/">url</a>.
 
<div class="col three">
	<img class="col three" src="/img/solr_result.png"/>
</div>

3) HDFS - You can either run `hadoop fs -ls /from_storm` or access namenode UI <a href="http://localhost:50070/">url</a>.

<div class="col three">
	<img class="col three" src="/img/hdfs_result.png"/>
</div>

I hope you got a fair idea about how to integrate Storm, Kafka, MongoDB, SOLR and HDFS for Realtime analysis. Although this was implemented in a single node cluster for learning purpose, it can be extended for multi-node scenarios as well. For further doubts and clarifications please comment below and I will respond as soon as possible. <br/>
<a href="/">Home</a>