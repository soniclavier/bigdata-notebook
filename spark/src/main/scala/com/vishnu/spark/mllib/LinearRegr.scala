package com.vishnu.spark.mllib

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LinearRegressionWithSGD


object LinearRegr {
  
  def main(args: Array[String]): Unit = {
    
    val conf = new SparkConf().setAppName("LinearRegression")
    val sc = new SparkContext(conf)
     
    val tf = new HashingTF(10000)
    
    val spam = sc.textFile("/spark_learning/spam.txt")
    val normal = sc.textFile("/spark_learning/normal.txt")
    
    val spamFeatures = spam.map(email=> tf.transform(email.split(" ")))
    val normalFeatures = normal.map(email=> tf.transform(email.split(" ")))
    
    val positiveLP = spamFeatures.map(features => LabeledPoint(1,features))
    val negativeLP = normalFeatures.map(features => LabeledPoint(0,features))
    
    val trainingData = positiveLP.union(negativeLP)
    trainingData.cache()
    
    val lr = new LinearRegressionWithSGD().setIntercept(true)
    val model = lr.run(trainingData)
    println(model.intercept+" "+model.weights)
    

    
  }
}