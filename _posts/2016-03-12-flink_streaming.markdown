---
layout: post
comments: true
title: Flink Streaming - Tumbling and Sliding Windows
date: 2016-03-12
PAGE_IDENTIFIER: flink_streaming
permalink: /flink_streaming.html
tags: ApacheFlink BigData Hadoop Scala
description: This article explains about the two types of Windows in Flink - Sliding windows and Tumbling windows
---
<div class="col three">
	<img class="col three" src="/img/flink_streaming/blog_header.png">
</div>
Flink has two types of Windows - **Tumbling Window** and **Sliding Window**. The main difference between these windows is that, Tumbling windows are non-overlapping where as Sliding windows **can be** overlapping. <br/>
In this article I will try to explain these two windows and will also show how to write Scala program for each of these. Code used in this blog is also available in my [github](https://github.com/soniclavier/hadoop_datascience/tree/master/flink/src/main/scala/com/vishnu/flink/streaming)

# **Need of Window**
In the case of Streaming applications, the data is continuous and therefore we can't wait for the whole data to be streamed before starting the processing. Off course we can process each incoming event as it comes and move on to the next one, but in some cases we will need to do some kind of aggregation on the incoming data - e.g,. how many users clicked a link in your web page over the last 10 minutes. In such cases we have to define a window and do the processing for the data within the window.

# **Tumbling Window**
A Tumbling window, tumbles over the stream of data. This type of Window is non-overlapping - i.e., the events/data in one window will not overlap/present in the other windows.
<div class="col three">
	<img class="col three" src="/img/flink_streaming/tumbling.png">
</div>
You can configure the window to tumble based on count - e.g., for every 5 elements, or based on time - e.g., for every 10 seconds.

# **Sliding Window**
A sliding window, opposed to a tumbling window, slides over the stream of data. Because of this a sliding window can be overlapping and it gives a smoother aggregation over the incoming stream of data - since you are not jumping from one set of input to the next, rather you are sliding over the incoming stream of data.
<div class="col three">
	<img class="col three" src="/img/flink_streaming/sliding.png">
</div>
Similar to Tumbling window, you can configure a Sliding window also to slide based on time or by count of events.

# **Scala Code - Tumbling Window**
Below example shows a word count program that listens to a socket and counts the number of times each word is received within a window. Window here is based on count and it tumbles for every 5 items.
{% highlight scala %}
object CountTumblingWindow {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment  
    val socTextStream = sev.socketTextStream("localhost",4444)  //read from socket
    val counts = socTextStream.flatMap{_.split("\\s")}  //split sentence into words
      .map { (_, 1) }  //emit 1 for each word	
      .keyBy(0)	 //group based on word
      .countWindow(5)  //window for every 5 items in the group
      .sum(1)						
      .setParallelism(4);  //setting parallelism (optional)
    counts.print()
    sev.execute()
  }
} 
{% endhighlight %}
In the above example, window is triggered for every 5 items. Since we are doing keyby, each window will be containing only words of the same group.

{% highlight sh %}
e.g.,
    if stream is : one two one two one two one two one
    window1 = {one,one,one,one,one}
    window2 = {two,two,two,two}
    window1 will triggered but not window 2, it need one more 'two' to reach count 5.
{% endhighlight %} 

# **Scala Code - Sliding Window**
In the below example, the window slides every 10 seconds and the width of the window is 15 seconds of data.
Therefore there is an overlap between the windows.
{% highlight scala %}
object TimeSlidingWindow {
  def main(args: Array[String]) {
    val sev = StreamExecutionEnvironment.getExecutionEnvironment
    val socTextStream = sev.socketTextStream("localhost",4444)
    val counts = socTextStream.flatMap{_.split("\\s")}
      .map { (_, 1) }
      .keyBy(0)
      .timeWindow(Time.seconds(15),Time.seconds(10))
      .sum(1).setParallelism(4);

    counts.print()
    sev.execute()
  }
}
{% endhighlight %}

That covers the basics on the types of Windows in Flink. There are various complex widowing operations that we can do in Flink - e.g., We can choose to Slide window based on time, but trigger the execution based on the Count of items and also choose to keep few of the items in the current window for the next window processing. I will try to cover these advanced topics in the upcoming blogs.

Thanks for reading!

<br/><a href="http://vishnuviswanath.com/">Home</a>