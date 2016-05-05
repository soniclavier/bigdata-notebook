package com.vishnu.spark.basics

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scala.collection.mutable.ListBuffer

object ApiLearn {

  def main(args: Array[String]): Unit = {

    /**
     * CONTEXT AND CONFIG
     */
    val sparkConf = new SparkConf().setAppName("APILearn")
    val sc = new SparkContext(sparkConf)

    //a. spark-submit --class com.vishnu.spark.basics.ApiLearn spark-vishnu-assembly-1.0.jar
    //b. spark-submit --class com.vishnu.spark.basics.ApiLearn --master spark://Vishnus-MacBook-Pro.local:7077 spark-vishnu-assembly-1.0.jar
    println(sc.appName) //a,b) APILearn
    println(sc.startTime) //1462038117646
    println(sc.applicationId) //local-1462038118983
    println(sc.defaultMinPartitions) //2
    println(sc.defaultMinSplits) //2
    println(sc.defaultParallelism) //a) 8 (equivalent to 8 cores in my sys) b) 2
    println(sc.isLocal) //a) true b)false 
    println(sc.sparkUser) //a,b) vishnu
    println(sc.master) //a)local[*] b)spark://Vishnus-MacBook-Pro.local:7077
    println(sc.version) //a,b) 1.6.0

    /**
     * RDD
     */
    val intRDD = sc.parallelize(List(1, 2, 3))
    val intRDD2 = sc.parallelize(List(4, 5, 6))
    //intRDD: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[2] at parallelize at <console>:21
    val anyRDD = sc.parallelize(List(1, "2", 3))
    //anyRDD: org.apache.spark.rdd.RDD[Any] = ParallelCollectionRDD[3] at parallelize at <console>:21

    //partitions of an rdd
    intRDD.partitions

    //union of 2 RDDs
    intRDD.union(intRDD2)

    //mapPartitionsWithIndex (e.g., to get a contents of a particular partition)
    val b = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9), 3)
    val part = b.partitions(0)
    val partRDD = b.mapPartitionsWithIndex((a, b) => if (a == part.index) b else Iterator(), true)

    //filter
    intRDD.filter(x => x > 2)
    val address = sc.parallelize(List(("123", "Street1", "City1"), ("432", "Street2", "City2")))
    address.filter { case (house, address, city) => city == "City2" }.collect

    //zipWithIndex - adds index for each RDD element
    address.zipWithIndex.collect
    //res47: Array[((String, String, String), Long)] = Array(((123,Street1,City1),0), ((432,Street2,City2),1))

    
    /*
     * FOREACH, FOREACH_PARTITION, MAP, MAP_PARTITION, MAP_PARTITION_WITHINDEX, FLATMAP
     */
    
    //foreachPartitions (returns nothing, can be used to perform some operation per partition, common example is DB connection
    address.foreachPartition { partition =>
      val dummyDB = new DummyDB
      //or any other operation that has to be done once per partition
      partition.foreach { case (a, b, c) => dummyDB.save(Address(a.toInt, b, c)) }
    }
    //note this does not print anything to console, but if you check the application stdout from the spark ui(localhost:8080) you can see the following
    //in printed 
    //123 saved
    //432 saved

    //for each item in address, do something. again, this does not print anything in the driver console instead prints in the stdout of worker
    address.foreach(x => println(x)) 
    
    //map vs foreach
    //foreach and map iterates through the elements in the given RDD (collection)
    //foreach does not return anything, instead it applies the given function on each element.
    //map applies the given function and returns the new/transformed collection
    address.map(x => (x._1, x))
    address.map(transform).collect //here transform is a function that takes a Tuple3 give another Tuple2 (f: T => U)
    
    //mapPartitions applies the given function to each partition. the function should be of the form, f: Iterator[T] => Iterator[U]
    address.mapPartitions(myIterator).collect
    address.mapPartitions{partition => partition.map(record=>(record._1))}.collect
    
    
    address.mapPartitionsWithIndex(myIterator2).collect
    /*
     * This will be executed per partition 4
		This will be executed per partition 3
		This will be executed per partition 1
		This will be executed per partition 6
		This will be executed per partition 0
		This will be executed per partition 5
		123
		This will be executed per partition 2
		This will be executed per partition 7
		432
     */
    
    //flatMap vs map, flatMap takes (f: T => TraversableOnce[U]), map takes (f: T => U) 
    val sentences = sc.parallelize(List("This is one sentence","Second sentence has five words",""))
    sentences.map(line=>line.split(" ").filter(word=>(word.contains("c")))).collect
    //res55: Array[Array[String]] = Array(Array(sentence), Array(Second, sentence), Array())
    
    //flatmap returns an traversable( an arrays of items) for each item in the collection[RDD in this case]. And flattens out the result
    sentences.flatMap(line=>line.split(" ").filter(word=>(word.contains("c")))).collect
    //res56: Array[String] = Array(sentence, Second, sentence)
    sentences.flatMap(sentenceSplitter)
    
    //groupBy, applies the given function on each element and adds that element to the group as returned by the function
    sentences.groupBy(x=> if (x.length > 10) "long" else "short").collect
    //res25: Array[(String, Iterable[String])] = Array((long,CompactBuffer(This is one sentence, Second sentence has five words, this is third sentence)))
    
    //soryBy
    //sorts the rdd based on the function.
    //in this example each sentence will be sorted based on the last two letters in the sentence
    sentences.sortBy(x=>x.substring(x.length-2,x.length)).collect
    
    val x = sc.parallelize(List(1,2,3,4))
    //fold
    //take an inital value and a function to be applied on the values. (x,y) are values
    //initial value will be applied multiple times (once per partition and once while combining the results)
    //e.g.,
    x.fold(0)((x,y)=>x+y) //10    
    
    x.fold(1)((x,y)=>x+y) //19 since I have 8 partitions
    
    val y = sc.parallelize(List(1,2,3,4),1)
    y.fold(1)((x,y)=>x+y) //12 since there is only 1 partition
    y.fold(2)((x,y)=>x+y) //14
    
    //aggregate, similar to combineByKey
    //scala currying is applied here,first parameter initializes the aggregation values (lets call this as aggr)
    //second fun defines what to do when you see a new value. (x,y) where x is the aggr and y is the new value
    //thrid fun defines how to combine two aggr's here x and y are of the type (Int,Int)
     x.aggregate(0,0)((x,y)=>(x._1+y,x._2+1),(x,y)=>(x._1+y._1,x._2+y._2))
     //res10: (Int, Int) = (10,4)

     //another example where aggr is of the type (Int)
     x.aggregate(0)((x,y)=>(x+y),(x,y)=>(x+y))
     //res11: Int = 10
    
    /*
     * REDUCE
     */
    //reduce walks through each pair of elements, it will perform the given fun on the pair and return result,
    // in the next iteration, i.e., for the nxt two elments, the left element will be the result of previous computation and right element will be the 
    // the next item in the collection
     val l = sc.parallelize(List(1,4,2,3,6,7))
     l.reduce((a,b)=>(a+b))
     //we cannot use map here, since map will return an org.apache.spark.rdd.RDD[Array[(String, Int)]], which does not have reduce by key
     sentences.flatMap(line=> line.split(" ").map(word => (word,1))).reduceByKey((v1,v2)=>(v1+v2)).collect
     
     sentences.saveAsTextFile("/somepath")
     sentences.saveAsObjectFile("/somepath")
      
      
     /*
      * PAIR RDD
      */
     //wordPair is a pair RDD
     val wordPair = sentences.flatMap(line => line.split(" ").map(word=>(word,1)))
     //groupByKey does not take any argument
     wordPair.groupByKey.collect
     //res21: Array[(String, Iterable[Int])] = Array((words,CompactBuffer(1)), (is,CompactBuffer(1, 1)), (five,CompactBuffer(1)), (has,CompactBuffer(1)), (sentence,CompactBuffer(1, 1, 1)), (Second,CompactBuffer(1)), (this,CompactBuffer(1)), (one,CompactBuffer(1)), (This,CompactBuffer(1)), (third,CompactBuffer(1)))
     
     
     //map values applies a function to each value in the RDD (k,v) pair
     wordPair.mapValues(x=>x+1).collect
     //res20: Array[(String, Int)] = Array((This,2), (is,2), (one,2), (sentence,2), (Second,2), (sentence,2), (has,2), (five,2), (words,2), (this,2), (is,2), (third,2), (sentence,2))
     
     //flatMap values, reutrn a list of values for each value and flattens out the result
     wordPair.flatMapValues(x=>List(x+1,x+2,x+3)).collect
     //res52: Array[(String, Int)] = Array((This,2), (This,3), (This,4), (is,2), (is,3), (is,4), (one,2), (one,3), (one,4), (sentence,2), (sentence,3), (sentence,4), (Second,2), (Second,3), (Second,4), (sentence,2), (sentence,3), (sentence,4), (has,2), (has,3), (has,4), (five,2), (five,3), (five,4), (words,2), (words,3), (words,4), (this,2), (this,3), (this,4), (is,2), (is,3), (is,4), (third,2), (third,3), (third,4), (sentence,2), (sentence,3), (sentence,4))
     
     //keys returns all the keys
     wordPair.keys.collect
     //res18: Array[String] = Array(This, is, one, sentence, Second, sentence, has, five, words, this, is, third, sentence)
     
     
     //values returns all the values
     wordPair.values.collect
     //res19: Array[Int] = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
     
     
     //sortByKey
     wordPair.sortByKey().collect
     //res51: Array[(String, Int)] = Array((Second,1), (This,1), (five,1), (has,1), (is,1), (is,1), (one,1), (sentence,1), (sentence,1), (sentence,1), (third,1), (this,1), (words,1))
     
     //combineByKey takes 3 functions
     //1. function decides what should happen when it sees a value for a key for the first time (parameter for the fun is the new value, 
     //what ever returned from this function becomes the structure of your combiner
     //2. function decides what to do when a value for a key already seen is found, the parameters for this function is the combiner and the new value
     //3. function tell how to combine two combiners
     //
     wordPair.combineByKey(initfun,mergeVal,mergeCombiner).collect
     //res56: Array[(String, Int)] = Array((words,1), (is,2), (five,1), (has,1), (sentence,3), (Second,1), (this,1), (one,1), (This,1), (third,1))
  }
  

  def initfun(x:Int) = {x}
  def mergeVal(x:Int,y:Int):Int ={x+y}
  def mergeCombiner(x:Int,y:Int):Int = {x+y}
  
  def sentenceSplitter(str:String) = {
    val parts = str.split(" ")
    parts.filter(wordFilter)
  }
  
  def wordFilter(word:String) = {
    word.contains("c")
  }

  def myIterator(iter: Iterator[(String,String,String)]) = {
    println("This will be executed per partition")
    var l = ListBuffer[(String,String)]()
    
    //iter has the elements that belong to this partition
    while(iter.hasNext) {
      val n:(String,String,String) = iter.next
      println(n._1)
      //this creates a 2 tuple
      l += (n._1->n._2)
    }
    //we have to return iterator since mapPartitions expects, f: Iterator[T] => Iterator[U]
    l.iterator
  }
  
  def myIterator2(index:Int, iter: Iterator[(String,String,String)]) = {
    println("This will be executed per partition "+index)
    var l = ListBuffer[(String,String)]()
    
    //iter has the elements that belong to this partition with index, index
    while(iter.hasNext) {
      val n:(String,String,String) = iter.next
      println(n._1)
      //this creates a 2 tuple
      l += (n._1->n._2)
    }
    //we have to return iterator since mapPartitions expects, f: Iterator[T] => Iterator[U]
    l.iterator
  }
  def transform(add:(String,String,String)) = {
    (add._1, Address(add._1.toInt,add._2,add._3))
  }

  case class Address(houseNumber: Int, street: String, city: String)
  class DummyDB {
    def save(item: Address) {
      println(item.houseNumber + " saved")
    }
  }
}