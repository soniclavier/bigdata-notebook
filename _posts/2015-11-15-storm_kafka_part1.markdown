---
layout: post
comments: true
title: Realtime Processing using Storm-Kafka- Part1
date: 2015-11-05
PAGE_IDENTIFIER: storm_kafka1
permalink: /realtime-storm-kafka1.html
tags: ApacheStorm MongoDB SOLR Hadoop BigData Java
description: This is the first of the three-part series of a POC on how to build a near Realtime Processing system using Apache Storm and Kafka in Java. In this first part, we will be dealing with setting up of the environment.
---
<div class="col three">
	<img class="col three" src="/img/storm_blog_header.png">
</div>

This is a three-part series of a POC on how to build a near Realtime Processing system using Apache Storm and Kafka in Java. So to give a brief introduction on how the system works, messages come into a Kafka topic, Storm picks up these messages using Kafka Spout and gives it to a Bolt, which parses and identifies the message type based on the header. Once the message type is identified, the content of the message is extracted and is sent to different bolts for persistence - SOLR bolt, MongoDB bolt or HDFS bolt.

In this first part, we will be dealing with setting up of the environment. If you already have the environment setup, you can jump to the <a href="http://vishnuviswanath.com/realtime-storm-kafka2.html">Part 2</a> which talks about how to setup the project in Eclipse and how to write the Bolts. Execution of the project and creation of Spout is discussed in the <a href="http://vishnuviswanath.com/realtime-storm-kafka3.html">Part 3</a>

The source code for this project is available in my <a href="https://github.com/soniclavier/hadoop/tree/master/stormkafka" target="blank">github</a>

### <b>Setup</b>
<span></span>	

For building this system, we would require

1. Hadoop <small>*2.6.0*</small>
2. Zookeeper <small>*3.4.6*</small>
3. Apache Kafka <small>*0.8.2.1*  Scala<sub>*2.9.1*</sub></small> 
4. Apache Solr <small>*5.3.1*</small>
5. MongoDB <small>*3.0.7*</small>
6. Apache Storm <small>*0.10.0*</small>

Versions I used are given in italics, It is not necessary to use the same versions but there might be some changes needed if versions are different.
*Note: All are single node setup*

#### **Hadoop**
I am assuming that hadoop is installed and I am not going through the installation steps here. If not you can do so easily by following the instructions <a href="https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html">here</a>.
After installation, start hadoop daemons and verify that all daemons have started by running jps command.
{% highlight sh %}
$ jps
12768 QuorumPeerMain
14848 SecondaryNameNode
15024 NodeManager
14949 ResourceManager
14758 DataNode
15067 Jps
14687 NameNode
{% endhighlight %}

#### **Zookeeper**
Zookeeper setup is also pretty straight forward, you can download zookeeper from <a href="http://www.eu.apache.org/dist/zookeeper/">here</a>.
Once downloaded, extract the archive and look for a zoo_sample.cfg inside conf folder. Copy it and change the name to zoo.cfg
{% highlight sh %}
$cp zoo_sample.cfg zoo.cfg
{% endhighlight %}
For single node setup most of the configurations in zoo_sample.cfg can be used as is, the only configuration that I usually change is `dataDir=/tmp/zookeeper`.
Create a new directory of your convenience and point *dataDir* to that directory to. e.g., `dataDir=/Users/vishnu/zookeeper_data`

Start the zookeeper server by running below command from zookeeper base directory.
{% highlight sh %}
$bin/zkServer.sh start
Starting zookeeper ... STARTED
{% endhighlight %}

#### **Apache Kafka**
Download the kafka binary from <a href="https://www.apache.org/dyn/closer.cgi?path=/kafka/0.8.2.1/kafka_2.9.1-0.8.2.1.tgz">here</a> and unpack it.
Now start the kafka broker by running below command from kafka base directory

{% highlight sh %}
$bin/kafka-server-start.sh config/server.properties
[2015-11-06 10:00:54,412] INFO Registered broker 0 at path /brokers/ids/0 with address 10.0.0.8:9092. (kafka.utils.ZkUtils$)
[2015-11-06 10:00:54,418] INFO [Kafka Server 0], started (kafka.server.KafkaServer)
{% endhighlight %}

config/server.properties holds the information about the kafka broker. Take a note of 
{% highlight properties %}
broker.id=0
port=9092
zookeeper.connect=localhost:2181
{% endhighlight %}

Now we need to create a kafka *topic* to which the message will be posted. Let's name this topic *'incoming'*. For creating a topic, we need to specify the topic name by `--topic`, zookeeper url by `--zookeeper`, number of partitions and replication factor. Run the below command for creating the topic.
{% highlight sh %}
$bin/kafka-topics.sh --create --topic incoming --zookeeper localhost:2181 --partitions 1 --replication-factor 1
Created topic "incoming"
{% endhighlight %}
Let's now test if the topic has be created successfully by posting and retrieving some messages.
Open a new terminal and run below command
{% highlight sh %}
$bin/kafka-console-consumer.sh --topic incoming --zookeeper localhost:2181 
{% endhighlight %}
In another terminal start a kafka console producer and send a sample message.
{% highlight sh %}
$bin/kafka-console-producer.sh --topic incoming --broker localhost:9092
hdfs testmessage
{% endhighlight %}
Check the terminal running the consumer if the message(*'hdfs testmessage'*) has been received.

#### **Apache Solr**
Download the solr distribution from <a href="http://www.apache.org/dyn/closer.lua/lucene/solr/5.3.1"> here</a> and unpack it.
Start the Solr server by running 
{% highlight sh %}
$bin/solr start
Waiting up to 30 seconds to see Solr running on port 8983 [/]  
Started Solr server on port 8983 (pid=15373). Happy searching! 
{% endhighlight %}
You can now access the Solr UI via <a href="http://localhost:8983/solr/#/">http://localhost:8983/solr/#/</a>

*Solr home page:*

<div class="col three">
	<img class="col three" src="/img/solr_loadpage.png">
</div>

Now we need to create a collection in solr. This will be used by our Storm topology to store the message of the type *solr*.
For creating a collection there are a set of configuration files needed, solr provides us basic configuration files which can be used for this. These files are available in
{% highlight sh %}
SOLR_BASE/server/solr/configsets/basic_configs/conf
{% endhighlight %}
Let's first create a new folder and copy the basic configs to it.
{% highlight sh %}
$mkdir server/solr/collection1
$cp -r server/solr/configsets/basic_configs/conf/ server/solr/collection1/conf
{% endhighlight %}
We need to change the default schema given by the basic configuration. To do that, open the file schema.xml in `server/solr/collection1/conf`

add the below line after `<field name="id" . . ./>`. This adds a field named *value* of the type *string*, it is a required attribute and is stored (*stored=true makes the field retrievable while doing search*). Indexed = false indicates that we are not going to do search on this field.
{% highlight xml %}
<field name="value" type="string" indexed="false" stored="true" required="true"/>
{% endhighlight %}

Now we have modified the schema as per our requirement, we will go ahead and create collection -  named *collection1*. To create the collection, run

{% highlight sh %}
$bin/solr create -c collection1
Creating new core 'collection1' using command:
http://localhost:8983/solr/admin/cores?action=CREATE&name=collection1&instanceDir=collection1

{
  "responseHeader":{
    "status":0,
    "QTime":539},
  "core":"collection1"}
{% endhighlight %}

You can view the collection via <a href="http://localhost:8983/solr/#/collection1">http://localhost:8983/solr/#/collection1</a>. Also, make sure that the fields *id* and *value* are created correctly from the dropdown in <a href="http://localhost:8983/solr/#/collection1/schema-browser">schema browser</a>.

#### **MongoDB**

Download and unpack mongodb from <a href="https://www.mongodb.org/downloads#production">here</a>. We need to create a folder which will be used by mongodb as data directory.
{% highlight sh %}
mkdir mongodb_data
{% endhighlight %}
Start the `mongod` daemon by running below command from mongodb installation folder. We need to pass the path of the data directory to mongod script.
{% highlight sh %}
bin/mongod --dbpath /Users/vishnu/mongodb_data
...
2015-11-07T17:51:05.223-0600 I STORAGE  [FileAllocator] done allocating datafile /Users/vishnu/mongodb_data/local.0, size: 64MB,  took 0.039 secs
2015-11-07T17:51:05.238-0600 I NETWORK  [initandlisten] waiting for connections on port 27017
{% endhighlight %}
Now we will be creating a database and a collection to store our messages.
Open another terminal and run below commands.

{% highlight sh %}
$ bin/mongo
MongoDB shell version: 3.0.7
connecting to: test
> use storm
switched to db storm
> db.createCollection("collection1");
{ "ok" : 1 }
{% endhighlight %}
`use storm` creates a new database called storm and switches to it. `createCollection("collection1")` creates a new collection named *'collection1'*

#### **Apache Storm**
Download Storm distribution from <a href="http://storm.apache.org/downloads.html">here</a> and unpack it. It is better to add the STORM_HOME/bin to you PATH. You can do so by changing your `bash_profile`. *Note: this step by might vary based on your OS. See how to set the path variable for <a href="http://stackoverflow.com/questions/14637979/how-to-permanently-set-path-on-linux">Linux</a> or <a href="http://hathaway.cc/post/69201163472/how-to-edit-your-path-environment-variables-on-mac">Mac</a>.

{% highlight sh %}
export STORM_HOME=/Users/vishnu/apache-storm-0.10.0
export PATH=$PATH/:$STORM_HOME/bin
{% endhighlight %}

We need to start the storm master - *nimbus* and the slave *supervisor*. Along with these we will also start Storm UI server and logviewer - this enables us to view the logs from storm ui 
{% highlight sh %}
$bin/storm nimbus
$bin/storm supervisor
$bin/storm ui
$bin/storm logviewer
{% endhighlight %}

Check <a href="http://localhost:8080/index.html">http://localhost:8080/index.html</a> and make sure that supervisor and the nimbus servers has been started.

<div class="col three">
	<img class="col three" src="/img/storm_home.png"/>
</div>

We have completed the environment setup and in the next part we will see how to setup the Eclipse project and start writing Storm Topology.

<a href="http://vishnuviswanath.com/realtime-storm-kafka2.html">Next</a>