package com.vishnu.spark.basics

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object SequenceFileTest {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SeqFileTest")
    val sc = new SparkContext(conf)
    
    //create a sequence file
    val data = sc.parallelize(List(("key1",1), ("key2",2)))
    data.saveAsSequenceFile("/usr/vishnu/spark_temp/seqfile_sample")
  }
  
  
}