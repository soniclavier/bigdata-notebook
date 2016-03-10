---
layout: post
comments: true
title: Getting Started With Apache Flink 1.0
date: 2016-03-09
PAGE_IDENTIFIER: flink_start
permalink: /flink_start.html
tags: ApacheFlink BigData Hadoop Scala
description: This is a quick guide on how to Setup Apache Flink 1.0
---
<div class="col three">
	<img class="col three" src="/img/flink/blog_header.png">
</div>
**Apache Flink** is the new star in the town. It is stealing the thunder from Apache Spark (at least in the streaming system) which has been creating buzz for some time now. This is because Spark streaming is built on top of RDDs which is essentially a collection, not a Stream. So now would be the right time to try your hands on Flink, even more so since Flink 1.0 was released last week.

In this short blog, I will explain you how to setup Flink in your system.	

# 1. Download Apache Flink
Go to [https://flink.apache.org/downloads.html](https://flink.apache.org/downloads.html). This page will show the latest stable release of Flink that is available for download(1.0 is latest currently). Under the binaries, click on Download according to your Hadoop version and Scala version. I am having Hadoop 2.6.0 and Scala 2.11
<div class="col three">
	<img class="col three" src="/img/flink/download.png">
</div>
If you don't have scala installed, you can install it by following instruction from [here](http://www.scala-lang.org/download/install.html).<br/> If you want to know your current scala version you can find it by running below command
{% highlight sh %}
$scala -version
Scala code runner version 2.11.7 -- Copyright 2002-2013, LAMP/EPFL
{% endhighlight %}

# 2. Start Flink
Start Flink jobmanager by running below command from the root folder of your Flink 
{% highlight sh %}
bin/start-local.sh 
Starting jobmanager daemon on host Vishnus-MacBook-Pro.local.
{% endhighlight %}

# 3. Flink dashboard
Flink has a pretty good UI where you can see details of your job, how many slots are present etc. You can access the Flink UI from [localhost:8081](localhost:8081)<br/>*Note: Spark UI also uses same port, make sure you don't have Spark running*
<div class="col three">
	<img class="col three" src="/img/flink/dashboard.png">
</div>

# 4. Flink Shell
Flink comes with a scala shell which can be started by running below command from Flink base folder
{% highlight sh %}
$bin/start-scala-shell.sh local
{% endhighlight %}

<div class="col three">
	<img class="col three" src="/img/flink/shell.png">
</div>

*Note:start-scala-shell.sh creates a mini cluster, you don't have to start separate jobmanager for scala shell to work on local mode*

Flink setup is complete. In the coming blogs, I will be writing more about how to write various Streaming applications using Apache Flink. Thanks for reading!
<br/><a href="http://vishnuviswanath.com/">Home</a>
