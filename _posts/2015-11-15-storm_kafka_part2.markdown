---
layout: post
comments: true
title: Realtime Processing using Storm-Kafka- Part2
date: 2015-11-05
PAGE_IDENTIFIER: storm_kafka_2
description: This is part two of the series Realtime Processing using Storm and Kafka. In this section we are going to create an Eclipse project and develop the Solr, MongoDb and Hdfs Bolt used for persisting the messages.
---
<div class="col three">
	<img class="col three" src="/img/storm_blog_header.png">
</div>

This is Part 2 of the series *Realtime Processing using Storm and Kafka*. If you have not read the first part, you can read it <a href="http://vishnuviswanath.com/2015/11/05/storm_kafka_part1.html">here</a>. In this section we are going to create an Eclipse project and develop the Solr, MongoDb and Hdfs Bolt used for persisting the messages.

The source code for this project is available in my <a href="https://github.com/soniclavier/hadoop/tree/master/stormkafka" target="blank">github</a>

### **Building Storm Topology**
<span></span>

* Language: Java
* IDE : Eclipse
* Build tool : Maven

Storm has mainly two components - *Spouts* and *Bolts*. 

#### Spouts
Spouts are the data sources for a topology. A spout reads data from an external source and emits them into the topology. There can me more than one spout in a topology reading data from different source (*e.g., twitter, tcp connection, kafka topic, flume). In this example, we will be creating  a Kafka spout which will be reading the messages coming into the topic <i>'incoming'</i> that we created during kafka setup.

#### Bolts
Bolts are the processing units of a topology. It can enrich the message, filter, persist into different sinks etc. In this example we will be creating four Bolts.

#### Topology
A topology is a network of Spouts and Bolts

1. *Sink-Type-bolt* : will act as a decision making node, by identifying the message type and sending it to the appropriate bolt for persistence.
2. *Solr-bolt* : for indexing into SOLR collection
1. *Hdfs-bolt* : for storing in HDFS
1. *Mongodb-bolt* : for saving in MongoDB collection

<div class="col three">
	<img class="col three" src="/img/storm_blog_flow.png">
</div>

#### **Creating the project**
Create a new maven project in eclipse and add the following dependencies in the pom.xml.

1. storm-core
2. kafka_2.9.1
3. storm-kafka
4. storm-hdfs
5. solr-solrj
6. json-simple

You can download the pom.xml from <a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/pom.xml">here</a>
<blockquote>
	Note: The artifact <b>slf4j-log4j12</b> has to be excluded from storm-core and kafka_2.9.1 dependency. Otherwise you might get <i>'multiple SLF4J bindings'</i> exception during execution.
</blockquote>

<blockquote>
	Note: We have to package the jar with all the dependencies except storm-core. It is better to use maven shade plugin rather than maven assembly plugin because the packaging done by assembly plugin may throw exception while submitting the jar to storm.
</blockquote>

#### Structure of the project
<div class="col three">
	<img class="col three" src="/img/project_structure.png"/>
</div>

#### Keys class
<a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/Keys.java">Keys.java</a> holds all the keys for the customizable properties of the topology.
e.g.,
{% highlight java %}
String KAFKA_SPOUT_ID          =  "kafka-spout";
String KAFKA_ZOOKEEPER         =  "kafka.zookeeper";
String KAFKA_TOPIC             =  "kafa.topic";
String KAFKA_ZKROOT            =  "kafka.zkRoot";
String KAFKA_CONSUMERGROUP     =  "kafka.consumer.group";
{% endhighlight %}

There is a default config file - `default_configs.properties` which will contain the default values for these properties. And this can be overriden by passing the path of some custom properties file. But the only condition is that, it should override all the properties defined in default_configs.properties. Below is a section of default_configs.properties
{% highlight properties %}
kafka-spout=kafka-spout
kafka.zookeeper=localhost:2181
kafa.topic=incoming
kafka.zkRoot=/kafka
kafka.consumer.group=sample_group
{% endhighlight %}

These properties will be loaded into a `Properties` object named `config` in the Toplogies class and can be accessed using the Keys class. e.g., to get the value of kafka spout id we can call
{% highlight java %}
configs.getProperty(Keys.KAFKA_SPOUT_ID)
{% endhighlight %}
<br/>


#### **Building the Bolts**
All the bolts are built by <a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/bolt/BoltBuilder.java">BoltBuilder.java</a>. It has methods for creating SinkTypeBolt, HdfsBolt, SolrBolt and MongoDB bolt.

#### SinkTypeBolt
<a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/bolt/SinkTypeBolt.java">SinkTypeBolt.java</a> extends BaseRichBolt. It has two important methods

1.declareOutputFields:

This method is used for declaring what are the output streams being emitted from this bolt and what will the fields be for each of the tuple it emits to those streams.
We are declaring 3 output streams here and each stream is going to have two fields 1) sinkType and 2) content. Topology.SOLR_STREAM, Topology.HDFS_STREAM etc are Strings used for naming these streams.
{% highlight java %}
public void declareOutputFields(OutputFieldsDeclarer declarer) {
	declarer.declareStream(Topology.SOLR_STREAM, new Fields( "sinkType","content" ));
	declarer.declareStream(Topology.HDFS_STREAM, new Fields( "sinkType","content" ));
	declarer.declareStream(Topology.MONGODB_STREAM, new Fields( "sinkType","content" ));
}
{% endhighlight %}
2.execute

Execute method receives a tuple at at time and does some processing. To make the example simple, it is assumed that our messages will have certain format i.e., it will be of the format `[type] [content]` where type will be either *solr*, *hdfs* or *mongo*. Also, SOLR and MongoDB messages will be of the format `fieldname:fieldvalue`; and there will be two fields - 1) id and 2) value. The execute method reads the tuple and extracts the type out of it. It then sends the content to any one of the streams by calling `collector.emit()`
{% highlight java %}
public void execute(Tuple tuple) {
	String value = tuple.getString(0);
	System.out.println("Received in SinkType bolt : "+value);
	int index = value.indexOf(" ");
	if (index == -1)
		return;
	String type = value.substring(0,index);
	System.out.println("Type : "+type);
	value = value.substring(index);
	if(type.equals("solr")) {
		collector.emit(Topology.SOLR_STREAM,new Values(type,value));
		System.out.println("Emitted : "+value);
	} else if (type.equals("hdfs")) {
		collector.emit(Topology.HDFS_STREAM,new Values(type,value));
		System.out.println("Emitted : "+value);
	} else if (type.equals("mongo")) {
		collector.emit(Topology.MONGODB_STREAM,new Values(type,value));
		System.out.println("Emitted : "+value);
	}
	collector.ack(tuple);	
}
{% endhighlight %}
As you can see based on the type, the value is emitted to their respective streams.

#### SolrBolt

<a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/bolt/SolrBolt.java">SolrBolt.java</a> receives a tuple, converts the tuple into a SolrInputDocument and send that document to SOLR server. Therefore it needs to know SOLR server url, which can be set through its constructor. 

1.prepare

During prepare, a new HttpSolrClient object is created using the solrAddress which was set through its constructor. 
<blockquote>Note: We are not creating the client object in the constructor because when a topology is submitted, the bolt object will be serialized and submitted and the class HttpSolrClient is non-serializable. If we initialize HttpSolrClient in the constructor, we will receive <b>java.io.NotSerializableException</b> exception. Where as the method <b>prepare</b> will be called only after the object is deserialized.</blockquote>
{% highlight java %}
public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
	this.collector = collector;
	this.solrClient = new HttpSolrClient(solrAddress);
}
{% endhighlight %}
2.getSolrInputDocumentForInput

This method is used for converting a tuple into SolrInputDocument, which is required for indexing the document onto SOLR.
{% highlight java %}
public SolrInputDocument getSolrInputDocumentForInput(Tuple input) {
	String content = (String) input.getValueByField("content");
	String[] parts = content.trim().split(" ");
	System.out.println("Received in SOLR bolt "+content);
	SolrInputDocument document = new SolrInputDocument();
	try {
		for(String part : parts) {
			String[] subParts = part.split(":");
			String fieldName = subParts[0];
			String value = subParts[1];
			document.addField(fieldName, value);
		}
	} catch(Exception e) {
		
	}
	return document;
}
{% endhighlight %}
3.execute

Execute method converts the input Tuple into a SolrInputDocument and sends it to SOLR server by calling commit()
<blockquote>Note: Ideally, we should not be committing each document, rather we should first buffer the documents and commit only once the buffer reaches a certain threshold.
</blockquote>
{% highlight java %}
public void execute(Tuple input) {
	SolrInputDocument document = getSolrInputDocumentForInput(input);
	try{
	solrClient.add(document);
	solrClient.commit();
	collector.ack(input);
	}catch(Exception e) {
	}
}
{% endhighlight %}


#### MongoDB Bolt
<a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/bolt/MongodbBolt.java">MongodbBolt.java</a> is similar to SolrBolt. It creates an instance of MongoClient using hostname and port, and then it creates an instance of MongoDatabase using ths MongoClient and the database name. Input tuple is converted into `org.bson.Document` by the method `getMongoDocForInput` and is inserted into the collection by

{% highlight java %}
mongoDB.getCollection(collection).insertOne(mongoDoc)
{% endhighlight %}

{% highlight java %}
public void execute(Tuple input) {	
	Document mongoDoc = getMongoDocForInput(input);
	try{
		mongoDB.getCollection(collection).insertOne(mongoDoc);
		collector.ack(input);
	}catch(Exception e) {
		e.printStackTrace();
		collector.fail(input);
	}
}

public Document  getMongoDocForInput(Tuple input) {
	Document doc = new Document();
	String content = (String) input.getValueByField("content");
	String[] parts = content.trim().split(" ");
	System.out.println("Received in MongoDB bolt "+content);
	try {
		for(String part : parts) {
			String[] subParts = part.split(":");
			String fieldName = subParts[0];
			String value = subParts[1];
			doc.append(fieldName, value);
		}
	} catch(Exception e) {
		
	}
	return doc;
}
{% endhighlight %}
<br/>


#### HdfsBolt

HdfsBolt in <a href="https://github.com/soniclavier/hadoop/blob/master/stormkafka/src/main/java/com/vishnu/storm/bolt/BoltBuilder.java">BoltBuilder.java</a> receives a tuple and saves the content on to HDFS. This bolt should be aware of the hdfs hostname and port. This should match host:port set by the property `fs.defaultFS` in `core-site.xml`. *FileNameFormat* specifies the name of the file that will be created in HDFS. *SyncPolicy* specifies how often should the data be synced/flushed to HDFS.
{% highlight java %}
public HdfsBolt buildHdfsBolt() {
	RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("|");
	SyncPolicy syncPolicy = new CountSyncPolicy(1000);
	FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f, Units.MB);
	FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath(configs.getProperty(Keys.HDFS_FOLDER));
	String port = configs.getProperty((Keys.HDFS_PORT));
	String host = configs.getProperty((Keys.HDFS_HOST));
	HdfsBolt bolt = new HdfsBolt()
    .withFsUrl("hdfs://"+host+":"+port)
    .withFileNameFormat(fileNameFormat)
    .withRecordFormat(format)
    .withRotationPolicy(rotationPolicy)
    .withSyncPolicy(syncPolicy);
	return bolt;
}
{% endhighlight %}	

In the next part of this series, we will develop the Kafka Spout, tie it all together using Storm Topology and execute the project.
<br/>
<a href="http://vishnuviswanath.com/2015/11/05/storm_kafka_part1.html">Previous</a> <a href="http://vishnuviswanath.com/2015/11/05/storm_kafka_part3.html">Next</a>