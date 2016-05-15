package com.vishnu.spark.mllib

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.feature.Normalizer

object FeatureTransformations {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("FeatureTransfomrations")
    val sc = new SparkContext(conf)
    
    val vectors = List(Vectors.dense(Array(-2.0,5.0,1.0)),Vectors.dense(Array(2.0,0.0,1.0)))
    val dataset = sc.parallelize(vectors)
    
    //with mean = true, with std = true
    val scaler = new StandardScaler(true,true)    
    val scalerModel = scaler.fit(dataset)
    scalerModel.transform(dataset).collect.foreach(println)
    
    val normalizer = new Normalizer()
    normalizer.transform(dataset).collect.foreach(println)
    
  }
}