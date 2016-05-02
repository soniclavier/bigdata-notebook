package com.vishnu.spark.basics

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

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