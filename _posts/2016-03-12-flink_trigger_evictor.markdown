---
layout: post
comments: true
title: Flink Streaming - Triggers and Evictors
date: 2016-03-14
PAGE_IDENTIFIER: flink_trigger_evictor
permalink: /flink_trigger_evictor.html
tags: ApacheFlink BigData Hadoop Scala
description: This article explains the concepts - Triggers and Evictors in Flink Streaming and how to impelemnt it using Scala.
---
<div class="col three">
	<img class="col three" src="/img/flink_trigger/blog_header.png">
</div>

In the last [blog](flink_streaming), we looked at the two basic types of Windows in Flink - Sliding and Tumbling windows. In the blog, I will explain you two importance concepts that can be used in Flink - **Triggers** and **Evictors**. 

# **Triggers**
Assume we have a sliding window *(of width 15 seconds, which slides every 10 seconds)* and we are collecting items in the window during streaming. A **trigger** can be used to tell Flink when to evaluate the function on the items in the window. For example, if you want the function to be evaluated on every 5 items that you receive within the window that we defined above, we can use `trigger(CountTrigger.of(5))`.

{% highlight scala %}
val counts = socTextStream.flatMap{_.split("\\s")}
  .map { (_, 1) }
  .keyBy(0)
  .window(SlidingProcessingTimeWindows.of(Time.seconds(15),Time.seconds(10)))
  .trigger(CountTrigger.of(5))
  .sum(1)
{% endhighlight %}

Let us consider few scenarios to understand trigger better.<br/>

<blockquote>Note 1: I am assuming that we are receiving the same word in the stream. This is done to make the explanation simple. Since there is a keyBy(0) after map, each word will belong to separate logical window grouped by the word.</blockquote>

<blockquote>Note 2: The sliding window used in this example is based on Processing time. Processing time is the time at which an event is processed in the system compared to EventTime which is the time at which event was created. I will be explaining these concepts in the upcoming blogs.</blockquote>

**scenario 1:**
<div class="col three">
	<img class="col three" src="/img/flink_trigger/trigger1.png">
</div>
This is the basic case, where window 1 received 5 items within its window-width of 15 seconds. Last two items have overlap with window 2, hence it will be present in both windows 1 and 2. But window 2 has only 2 items which is less than the trigger count **5**. Whereas window 1 has received 5 items within it's window-width and hence the function `sum()` will be triggered.

**scenario 2:**
<div class="col three">
	<img class="col three" src="/img/flink_trigger/trigger2.png">
</div>
In this case, the items arrived in such a way that, both windows 1 and 2 received 5 items in the region where it overlaps. Hence, both windows will be triggered at the same time.

**scenario 3:**
<div class="col three">
	<img class="col three" src="/img/flink_trigger/trigger3.png">
</div>
This is similar to scenario 2, except that window 1 received 10 items, 5 of which are overlapping with window 2. What do you think will happen in such scenario?


# **Evictors**
An evictor is used to remove some items from the window before the window function is called. Let us add an evictor to our trigger example.

{% highlight scala %}
val counts = socTextStream.flatMap{_.split("\\s")}
  .map { (_, 1) }
  .keyBy(0)
  .window(SlidingProcessingTimeWindows.of(Time.seconds(15),Time.seconds(10)))
  .trigger(CountTrigger.of(5))
  .evictor(CountEvictor.of(3))  //evictor
  .sum(1)
{% endhighlight %}

Here, the evictor is CountEvictor of 3, i.e., it will evict all the items except 3 from the window once the trigger is fired. e.g.,
Consider the **scenario 1** of trigger example and assume we added a CountEvictor of 3 to it.
<div class="col three">
	<img class="col three" src="/img/flink_trigger/evictor.png">
</div>

The function sum will be applied only to the 3 items which are left in the window after eviction.

That conludes this post, you can find the code used in this aritcle in my [GitHub](https://github.com/soniclavier/hadoop_datascience/tree/master/flink/src/main/scala/com/vishnu/flink/streaming). Thanks for reading!
<br/><a href="http://vishnuviswanath.com/">Home</a>