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
    //intRDD: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[2] at parallelize at <console>:21
    val anyRDD = sc.parallelize(List(1, "2", 3))
    //anyRDD: org.apache.spark.rdd.RDD[Any] = ParallelCollectionRDD[3] at parallelize at <console>:21

    //partitions of an rdd
    intRDD.partitions
    
    
  }
}