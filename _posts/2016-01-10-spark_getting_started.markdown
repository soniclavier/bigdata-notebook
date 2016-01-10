---
layout: post
comments: true
title: Getting Started With Apache Spark 1.6
date: 2016-01-10
PAGE_IDENTIFIER: spark_getting_started
permalink: /spark_start.html
description: This is quick guide on how to Setup Apache Spark 1.6 with YARN.
---
<div class="col three">
	<img class="col three" src="/img/spark_start/heading.png">
</div>

In this short blog, I will explain how to setup Apache Spark 1.6 with YARN. I assume Hadoop is already installed.

# 1. Download Apache Spark
Go to [spark.apache.org/downloads.html](http://spark.apache.org/downloads.html) and choose the Spark release you want to download(1.6.0 is the default currently). Then under the package type choose the Spark release corresponding to your Hadoop version. Mine is 2.6 hence I choose *Pre-built for Hadoop 2.6 and later*
<div class="col three">
  <img class="col three" src="/img/spark_start/spark_download.png">
</div>
If you want, you can download the Source Code, navigate to the base folder and build it based on your Hadoop version using below command.
{% highlight sh %}
mvn -Pyarn -Phadoop-2.6 -Dhadoop.version=2.6.0 -DskipTests clean package
{% endhighlight %}

# 2. Set HADOOP_CONF_DIR
To run Spark in YARN mode, we need to set the HADOOP_CONF_DIR environment variable.
{% highlight sh %}
$ vi ~/.bash_profile
export HADOOP_HOME=/Users/vishnu/hadoop-2.6.0
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
$ source ~/.bash_profile
{% endhighlight %}


# 3. Start the Master
Run the start-master.sh which is located in the sbin folder of Spark. This will start the Spark master at IP:8080
check [http://localhost:8080](http://localhost:8080) or http://yourip:8080

{% highlight sh %}
sbin/start-master.sh
{% endhighlight %}

From the Spark UI, copy the Master URL, which in my case is *spark://Vishnus-MacBook-Pro.local:7077*
<div class="col three">
  <img class="col three" src="/img/spark_start/spark_master.png">
</div>

# 4. Start the Slave
Run the start-slave.sh which is located in the sbin folder. Pass the Master URL copied in the previous step as argument to the start-slave.sh script. This will start the Slave/Worker. 
{% highlight sh %}
sbin/start-slave.sh sbin/start-slave.sh spark://Vishnus-MacBook-Pro.local:7077
{% endhighlight %}
Go back to your Spark UI and you can see that *Alive Workers* is now 1 and the worker details are displayed under *Workers*.
<div class="col three">
  <img class="col three" src="/img/spark_start/spark_slave.png">
</div>

# 5. Start Spark Shell
Run the spark-shell.sh file located in the bin folder of Spark with *'--master'* as *'yarn'*
{% highlight sh %}
bin/spark-shell --master yarn
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 1.6.0
      /_/

Using Scala version 2.10.5 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_60)
Type in expressions to have them evaluated.
Type :help for more information.
Spark context available as sc.
SQL context available as sqlContext.
scala>
{% endhighlight %}

Spark setup is complete. Thanks for reading.<br/>
<a href="http://vishnuviswanath.com/">Home</a>
