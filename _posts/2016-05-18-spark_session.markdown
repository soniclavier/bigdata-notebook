---
layout: post
comments: true
title: Experiment with Spark 2.0 - Session
date: 2016-03-14
PAGE_IDENTIFIER: spark_session
permalink: /spark_session.html
tags: ApacheSpark BigData Hadoop Scala
description: This blog post is about my experiment with Spark 2.0, Sessions
---
<div class="col three">
	<img class="col three" src="/img/spark_session/blog_header.png">
</div>
The first thing that I noticed when I started the spark shell in Spark 2.0 is this line - `Spark session available as 'spark'.`
<div class="col three">
  <img class="col three" src="/img/spark_session/spark_session.png">
</div>
You would not have seen this in previous versions of Spark.

So what is this SparkSession? To find out more, I cloned the latest copy of Spark from [spark github repository](https://github.com/apache/spark) and opened it in my IntelliJ
<div class="col three">
  <img class="col three" src="/img/spark_session/spark_class.png">
</div>

So, **SparkSession** is new entry point from Spark 2.0. Prior to 2.0, we had only SparkContext and SQLContext, and also we would create StreamingContext (if using streaming). 
It looks like SparkSession is part of the Spark's plan of unifying the APIs from Spark 2.0. *(I may be wrong)*

getOrCreate method of SparkSession builder does the following:

1. ***Create a SparkConf***
2. ***Get a SparkContext*** (using SparkContext.getOrCreate(sparkConf))
3. ***Get a SparkSession*** (using SQLContext.getOrCreate(sparkContext).sparkSession) 

Once spark session is created, it can be used to read data from various sources.

<blockquote>Note : All the commands used in the blog post can be found <a href="https://github.com/soniclavier/hadoop_datascience/blob/master/spark/src/main/scala/com/vishnu/spark/blog/supportfiles/spark_session_blog_commands">here</a></blockquote>
{% highlight scala %}
spark.read.     //pressed tab here
csv   format   jdbc   json   load   option   options   orc   parquet   schema   stream   table   text
//Load some json file
val df = spark.read.json("/spark_learning/pandainfo.json")
df.show
+--------------------+-----------+---------------+
|               knows|lovesPandas|           name|
+--------------------+-----------+---------------+
|                null|       true|Sparky The Bear|
|                null|       null|         Holden|
|[WrappedArray(hol...|       true|Sparky The Bear|
+--------------------+-----------+---------------+
{% endhighlight %}
<blockquote>Note: I am using the dataset from <a href="https://github.com/databricks/learning-spark/tree/master/files">learning-spark</a> github repository.</blockquote>
Let us now register this Dataframe as a temp table
{% highlight scala %}
df.registerTempTable("pandas")
warning: there was one deprecation warning; re-run with -deprecation for details 
{% endhighlight %}
It looks like `registerTempTable` method is deprecated. To find out the alternative method to use, I checked the class `Dataset.scala`.
<div class="col three">
  <img class="col three" src="/img/spark_session/temp_table_depricated.png">
</div>
{% highlight scala %}
df.createOrReplaceTempView("pandas")
{% endhighlight %}

### **spark.table**
You can access the registered table via 
{% highlight scala %}
spark.table("pandas")
//also we can run sql queries
//this we used to do using SQLContext in earlier versions
//using sqlContext.sql("query here")
spark.sql("select name from pandas").show 
+---------------+
|           name|
+---------------+
|Sparky The Bear|
|         Holden|
|Sparky The Bear|
+---------------+
{% endhighlight %}

### **spark.udf**
We can register udf(User Defined Function) using the SparkSession.
{% highlight scala %}
spark.udf.register("addone",(x:Int)=>x+1)
{% endhighlight %}

### **createDataSet**
This API is similar to how we create an RDD using SparkContext
{% highlight scala %}
scala> val ds = spark.createDataset(List(1,2,3))   //from a List
ds: org.apache.spark.sql.Dataset[Int] = [value: int]

scala> val rdd = sc.parallelize(List(1,2,3))
scala> val ds = spark.createDataset(rdd) //from RDD
ds: org.apache.spark.sql.Dataset[Int] = [value: int]
{% endhighlight %}

### **createDataFrames**
Used for creating DataFrames. We cannot create a Dataframe from our earlier RDD[Int] because createDataFrame requires an `RDD[A <: Product]` - i.e., a class that is subclass of Product. So we will create a DataFrame from an RDD of case class.
{% highlight scala %}
case class Num(x:Int)
val rdd = sc.parallelize(List(Num(1),Num(2),Num(3)))
spark.createDataFrame(rdd).show
+---+
|  x|
+---+
|  1|
|  2|
|  3|
+---+
{% endhighlight %}

Let us look at one more way of creating DataFrame, using Row RDD and Schema
{% highlight scala %}
import org.apache.spark.sql.types.{StructType,StructField,IntegerType};
import org.apache.spark.sql.Row
val rowRDD = rdd.map(x=>Row(x))
val schema = StructType(Array(StructField("num", IntegerType, true)))
spark.createDataFrame(rowRDD,schema).show
+---+
|num|
+---+
|  1|
|  2|
|  3|
+---+
{% endhighlight %}

### **Catalog**
Catalog provides a catalog of information about the databases and tables in the session, also some actions like drop view, cacheTable, clearCache etc

{% highlight scala %}
spark.catalog.cacheTable("pandas") // caches the table into memory, throws Table or view  not found in database exeception if not found.
spark.catalog.uncacheTable("pandas")  // to remove table from memory

spark.catalog.currentDatabase
res4: String = default

spark.catalog.isCached("pandas")
res24: Boolean = true

spark.catalog.clearCache 

spark.catalog.listDatabases.take(1)
res29: Array[org.apache.spark.sql.catalog.Database] = Array(Database[name='default', description='Default Hive database', path='hdfs://localhost:9000/Users/vishnu/spark-2.0.0-S
NAPSHOT-bin-hadoop2.6/spark-warehouse'])

spark.catalog.listTables("default").take(1)
res30: Array[org.apache.spark.sql.catalog.Table] = Array(Table[name='pandas', tableType='TEMPORARY', isTemporary='true'])

spark.catalog.dropTempView("pandas") //drops the table
{% endhighlight %}

This concludes my experiments with SparkSession for now. I will try to explore more about the new features in Spark 2.0 and share with you in later posts!
<br/><a href="search.html?query=spark">Continue reading</a>