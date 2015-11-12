---
layout: post
comments: true
title: Realtime Processing using Storm-Kafka Part1
date: 2015-11-05
PAGE_IDENTIFIER: spark_scala
description: This is the first of the three-part series of a POC on how to build a near Realtime Processing system using Apache Storm and Kafka in Java. In this first part, we will be dealing with setting up of the environment.
---
<div class="col three">
	<img class="col three" src="/img/storm_blog_header.png">
</div>

This is a three-part series of a POC on how to build a near Realtime Processing system using Apache Storm and Kafka in Java. So to give a brief introduction on how the system works, messages come into a Kafka topic, Storm picks up these messages using Kafka Spout and gives it to a Bolt, which parses and identifies the message type based on the header. Once the message type is identified, the content of the message is extracted and is sent to different bolts for persistence - SOLR bolt, MongoDB bolt or HDFS bolt.

In this first part, we will be dealing with setting up of the environment. If you already have the environment setup, you can jump to the **[Part 2]({% post_url 2015-11-15-storm_kafka_part2%})** which talks about how to setup the project in Eclipse and how to write the Bolts. Execution of the project and creation of Spout is discussed in the **[Part 3]({% post_url 2015-11-15-storm_kafka_part3%})**

The source code for this project is available in my <a href="https://github.com/soniclavier/hadoop/tree/master/stormkafka" target="blank">github</a>
