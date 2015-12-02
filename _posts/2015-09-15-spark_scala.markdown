---
layout: post
comments: true
title: Spark MapReduce and Scala underscore
date: 2015-09-15
permalink: /spark-scala.html
PAGE_IDENTIFIER: spark_scala_blog
description: A blog on how to write Spark mapreduce and an introduction on Scala underscore
---
<div class="col three">
	<img class="col three" src="/img/spark_blog.jpg">
</div>
This is a basic guide on how to run map-reduce in Apache Spark using Scala. I will also try to explain the basics of Scala underscore, how it works and few examples of writing map-reduce programs with and without using underscore. 

The source code is available <a href="https://github.com/soniclavier/hadoop/blob/master/map_reduce_in_spark.scala" target="blank">here</a>

### <b>MapReduce</b>
The first step is to create an RDD(Resilient Distributed Dataset) of input Strings. An RDD is a collection of data which is partitioned, it is similar to a distributed collection. The more the number of partitions in an RDD, the more the parallelism. When a job runs, each partition will be moved to the node where it is going to be processed.

{% highlight scala %}
val lines = sc.parallelize(List("this is","an example"))
lines: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[0] at parallelize at <console>:21
{% endhighlight %}

<i>sc</i>, is the spark context which is available by default in the spark-shell and <i>sc.parallelilize</i> will parallelize the input which is a list of two Strings in this case. 

### <b>Map</b>
Now that we have the RDD, we will run a <i>map()</i> operation on it.

{% highlight scala %}
val lengthOfLines = lines.map(line => (line.length))
lengthOfLines: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[2] at map at <console>:23
lengthOfLines.collect()
res0: Array[Int] = Array(7, 10)
{% endhighlight %}

Here the map operation executes given function (to find the length) on each the element in the RDD and returns a new RDD. <i>lengthOfLines.collect()</i> operation shows that the result is an array with elements 7 and 10.

### <b>Reduce</b>
Let us run the reduce operation on the result we obtained from map.
{% highlight scala %}
lines.map(line => (line.length)).reduce((a,b) => a + b)
res1: Int = 17
{% endhighlight %}

<i>reduce()</i> operation in Spark is a bit different from how the Hadoop MapReduce used to be, reduce() in spark produces only single output instead of producing key-value pairs. In the above reduce operation the length of each line, is summed up to obtain the result, 17. We will later go through <i>reduceByKey()</i> operation which is similar to the reduce in Hadoop MapReduce.

Let's take another example of reduce() 

{% highlight scala %}
val lines = sc.parallelize(List("this is","an example"))
val firstWords = lines.map(line => (line.substring(0,line.indexOf(" ")))).collect()
firstWords: Array[String] = Array(this, an)
firstWords.reduce((a,b) => a +" "+b)
res2: String = this an
{% endhighlight %}

In this example, we took two lines, did a substring operation to obtain only the first word of each line and then concatenated in reduce. The point to note here is that, reduce() operation always returns a single result - string "this an" in this example. 
<blockquote>Here we ran reduce operation on an Array(firstWords) which is not on an RDD. reduceByKey() can only called on an RDD where as reduce() can be called even if the object is not an RDD.</blockquote>

### <b>Scala Underscore</b>
Now, let us come back to the reduce operation and see how we can re-write it using underscore( _ ). Here reduce takes two arguments a and b and does a summation. 

{% highlight scala %}
lines.map(line => (line.length)).reduce((a,b) => a + b)
{% endhighlight %}

Using Scala underscore, this can also be written as

{% highlight scala %}
lines.map(line => (line.length)).reduce(_ + _)
{% endhighlight %}

((a,b) => a+b) can be re-written as <b>(_ + _)</b> : here it is implicitly understood that the function takes two parameters and does a <b>"+"</b> on them.

(line => (line.length)) can be re-written as <b>_.length</b> : map is taking a single parameter- line as input and doing a.length on it, so the _ here becomes the only parameter and _.length finds the length of the line.

### <b>Word count using flatMap and reduceByKey</b>
One of the difference between flatMap() and map() is that, map should always return a result where as flatMap need not. Have a look at the below examples

{% highlight scala %}
val lines = sc.parallelize(List("this is line number one","line number two","line number three"))
lines.flatMap(_.split(" ").filter(word => word.contains("this")).map(word => (word,1))).collect()
res83: Array[(String, Int)] = Array((this,1))
lines.map(_.split(" ").filter(word => word.contains("this")).map(word => (word,1))).collect()
res85: Array[Array[(String, Int)]] = Array(Array((this,1)), Array(), Array())
lines.map(line => (line.length)).reduce(_ + _)
{% endhighlight %}

We have three strings and we are doing a filtering based on the content. The result we got from the flatMap after filtering is `Array((this,1))` where as the map operation returned `Array(Array((this,1)), Array(), Array())` -two empty arrays.

<blockquote>return type of flatMap and map is an RDD not an array, the above result with array was obtained after calling collect() on the RDD returned by map operations.</blockquote>

Another difference between flatMap and map is that, flatMap flattens out the result, i.e., if you are getting an Array of Array of String in map, in flatMap you will get Array of String. See the below example

{% highlight scala %}
val lines = sc.parallelize(List("line number one","line number two"))
lines: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[10] at parallelize at <console>:21
val words = lines.map(line => (line.split(" ")))
words: org.apache.spark.rdd.RDD[Array[String]] = MapPartitionsRDD[11] at map at <console>:23
words.collect()
res6: Array[Array[String]] = Array(Array(line, number, one), Array(line, number, two))
val words = lines.flatMap(line => (line.split(" ")))
words: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[12] at flatMap at <console>:23
words.collect()
res7: Array[String] = Array(line, number, one, line, number, two)
{% endhighlight %}

<i>reduceByKey()</i> takes a function that accepts two values and produces one. Here the result is an RDD of (key,value) pairs in contrast to reduce() where the result was just one value.

Let's move on to the word count program. We will be using flatMap and reduceByKey explained earlier

{% highlight scala %}
val lines = sc.parallelize(List("line number one","line number two"))
lines.collect()
res8: Array[String] = Array(line number one, line number two)
val words = lines.flatMap(line => (line.split(" ")))
words.collect()
res11: Array[String] = Array(line, number, one, line, number, two)
val wordCount = words.map(x => (x,1)).reduceByKey(_ + _)
wordCount.collect()
res12: Array[(String, Int)] = Array((number,2), (two,1), (line,2), (one,1))
{% endhighlight %}

In the first line, we are creating an RDD with two Strings. Next, we are splitting the line based on space. Then for each word, the mapper will emit key as the word and 1 as the value. The reducer will receive these key,value pairs and will do an aggregation of all the values to find the number of occurrences of each word.