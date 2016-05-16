package com.vishnu.spark.basics

import org.apache.spark.Partitioner
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext


class MyPartitioner extends Partitioner{
  def numPartitions = 10
  
  def getPartition(key: Any): Int = {
    key match {
      case s: String => s.length()%numPartitions
    }
  }
}

object CustomPartitioner {
  
  val conf = new SparkConf().setAppName("CustomPartitioner")
  val sc = new SparkContext(conf)
  
  val rdd = sc.parallelize(List("word","stream","sql","dataframe","auction","averylongword","anotherveryverylongword"))
  val myPart = new MyPartitioner
  
  val pairRdd = rdd.map(word=>(word,1))
  val partitionedRdd = pairRdd.partitionBy(myPart)
  
}