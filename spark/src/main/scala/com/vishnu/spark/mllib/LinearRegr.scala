package com.vishnu.spark.mllib

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.linalg.Vectors


object LinearRegr {
  
  def main(args: Array[String]): Unit = {
    
    val conf = new SparkConf().setAppName("LinearRegression")
    val sc = new SparkContext(conf)
     val sqlContext = new SQLContext(sc)
    
    val features = Array("price","numBeds","year","sqft")
    val path = "/spark_learning/house_data.csv"
    val housePrice = sc.textFile(path).map(line => Vectors.dense(line.split(",").map(_.toDouble)))
    
    val houseFeaturesLP = housePrice.map(house => LabeledPoint(house(0).toLong,house))
    
  
    val lrModel = LinearRegressionWithSGD.train(houseFeaturesLP,10)
    
    println(lrModel.intercept+" "+lrModel.weights)
    
    val entry = "0,5,2016,4000"
    val newEntry = LabeledPoint(0,Vectors.dense(entry.split(",").map(_.toDouble)))
    println(lrModel.predict(newEntry.features))
    

    
  }
}