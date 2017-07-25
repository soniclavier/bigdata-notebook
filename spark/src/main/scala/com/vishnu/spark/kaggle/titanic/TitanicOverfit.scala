package com.vishnu.spark.kaggle.titanic

import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.linalg.Vectors

import scala.reflect.runtime.universe



object TitanicOverfit {

  def prepareOverfitData(data: DataFrame, train: Boolean,sqlContext: SQLContext): DataFrame = {
    val toDouble = sqlContext.udf.register("toDouble",((n:Int) => {n.toDouble}))
    var preparedData = data.drop("Name").drop("Ticket")
    if (train)
      preparedData = preparedData.drop("PassengerId")
    else
      preparedData = preparedData.withColumn("PassengerId", toDouble(preparedData("PassengerId")))
    var indexer = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex").fit(preparedData)
    preparedData = indexer.transform(preparedData)
    indexer = new StringIndexer().setInputCol("Cabin").setOutputCol("CabinIndex").fit(preparedData)
    preparedData = indexer.transform(preparedData)
    indexer = new StringIndexer().setInputCol("Embarked").setOutputCol("EmbarkedIndex").fit(preparedData)
    preparedData = indexer.transform(preparedData)
    preparedData = preparedData.drop("Sex").drop("Cabin").drop("Embarked")

    if (train)
      preparedData = preparedData.withColumn("Pclass", toDouble(preparedData("Pclass"))).withColumn("SibSp", toDouble(preparedData("SibSp"))).withColumn("Parch", toDouble(preparedData("Parch"))).withColumn("Survived", toDouble(preparedData("Survived")))
    else
      preparedData = preparedData.withColumn("Pclass", toDouble(preparedData("Pclass"))).withColumn("SibSp", toDouble(preparedData("SibSp"))).withColumn("Parch", toDouble(preparedData("Parch")))
    return preparedData
  }
  def main(args: Array[String]) {
    
    val spark = SparkSession.builder().
      appName("Titanic Overfit").
      master("spark://Vishnus-MacBook-Pro.local:7077").
      getOrCreate()

    import spark.implicits._

    val sc = spark.sparkContext
    val sqlContext = spark.sqlContext
    var train_data = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/kaggle/titanic/train.csv").toDF("PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")
    var prepared_train = prepareOverfitData(train_data, true,sqlContext)
    var trainLabeled = prepared_train.map { row: Row =>
      val features = Array[Double](row(1).asInstanceOf[Double],
        row(2).asInstanceOf[Double],
        row(3).asInstanceOf[Double],
        row(4).asInstanceOf[Double],
        row(5).asInstanceOf[Double],
        row(6).asInstanceOf[Double],
        row(7).asInstanceOf[Double],
        row(8).asInstanceOf[Double])
      LabeledPoint(row(0).asInstanceOf[Double], Vectors.dense(features))
    }

    //val splits = trainLabeled.randomSplit(Array(0.6, 0.4), seed = 11L)
    //val training = splits(0).cache()
    //val test = splits(1)
    
    val categoricalFeaturesInfo = Map[Int, Int]()
    //val model = RandomForest.trainClassifier(processed_data,2, categoricalFeaturesInfo, 5, "auto", "gini", 4, 32)
    
    //val model = RandomForest.trainClassifier(trainLabeled,2, categoricalFeaturesInfo, 5, "auto", "gini", 4, 32)
    val model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(trainLabeled.rdd)
    

    /*
    val predictionAndLabels = test.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (prediction.toDouble, label.toDouble)
    }

    val metrics = new MulticlassMetrics(predictionAndLabels)
    val precision = metrics.precision
    println("Precision = " + precision) 
    */

    model.save(sc, "/kaggle/titanic/overfit_model")

    var submission_data = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/kaggle/titanic/test.csv").toDF("PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")
    var prepared_submission = prepareOverfitData(submission_data, false,sqlContext)
    var submissionLabeled = prepared_submission.map { row: Row =>
      val features = Array[Double](row(1).asInstanceOf[Double],
        row(2).asInstanceOf[Double],
        row(3).asInstanceOf[Double],
        row(4).asInstanceOf[Double],
        row(5).asInstanceOf[Double],
        row(6).asInstanceOf[Double],
        row(7).asInstanceOf[Double],
        row(8).asInstanceOf[Double])
      LabeledPoint(row(0).asInstanceOf[Double], Vectors.dense(features))
    }
    
    val submissionPrediction = submissionLabeled.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (label.toInt, prediction.toInt)
    }
    
    submissionPrediction.rdd.saveAsTextFile("/kaggle/titanic/overfit_output")
  }
}