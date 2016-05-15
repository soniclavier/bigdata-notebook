package com.vishnu.spark.mllib

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.feature.IDF

object TFIDF {
  
  def main(args: Array[String]): Unit = {
    
   val conf = new SparkConf().setAppName("TFIDF")
    val sc = new SparkContext(conf)
    
    val tf = new HashingTF()
    
    val docs = sc.wholeTextFiles("/spark_learning")    
    val wordsRDD: RDD[Seq[String]] = docs.map{case (name,content)=> content.split(" ")}
    println(wordsRDD.take(3))
    val tfVectors = tf.transform(wordsRDD).cache
    
    val idf = new IDF()
    val idfModel = idf.fit(tfVectors)
    val tfIdfVectors = idfModel.transform(tfVectors)
    

  }
}