package com.vishnu.spark.kaggle

import org.apache.spark.ml.feature.{ OneHotEncoder, StringIndexer }
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.sql.SQLContext
import org.apache.spark.ml.feature.Tokenizer
import org.apache.spark.ml.feature.HashingTF
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.Pipeline
import org.apache.spark.mllib.classification.{ LogisticRegressionWithLBFGS, LogisticRegressionModel }
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.DataFrame
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

object TitanicUnderfit {

  /**
   * Keep age as the only feature.
   */
  def prepareUnderfitData(data: DataFrame, train: Boolean, sqlContext: SQLContext): DataFrame = {
    val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))
    var preparedData = data.drop("Pclass").drop("Name").drop("SibSp").drop("Parch").drop("Ticket").drop("Fare").drop("Cabin").drop("Embarked")
    if (train)
      preparedData = preparedData.drop("PassengerId")
    else
      preparedData = preparedData.withColumn("PassengerId", toDouble(preparedData("PassengerId")))
    var indexer = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex").fit(preparedData)
    preparedData = indexer.transform(preparedData)
    preparedData = preparedData.drop("Sex")
    if (train)
      preparedData = preparedData.withColumn("Survived", toDouble(preparedData("Survived")))
    return preparedData
  }

  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("Titanic Underfit").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    var train_data = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/kaggle/titanic/train.csv").toDF("PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")
    var prepared_train = prepareUnderfitData(train_data, true, sqlContext)
    var trainLabeled = prepared_train.map { row: Row =>
      val features = Array[Double](row(1).asInstanceOf[Double],
        row(2).asInstanceOf[Double])
      LabeledPoint(row(0).asInstanceOf[Double], Vectors.dense(features))
    }

    //val splits = trainLabeled.randomSplit(Array(0.6, 0.4), seed = 11L)
    //val training = splits(0).cache()
    //val test = splits(1)

    val model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(trainLabeled)

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
    model.save(sc, "/kaggle/titanic/underfit_model")

    var submission_data = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/kaggle/titanic/test.csv").toDF("PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")
    var prepared_submission = prepareUnderfitData(submission_data, false,sqlContext)
    var submissionLabeled = prepared_submission.map { row: Row =>
      val features = Array[Double](row(1).asInstanceOf[Double],
        row(2).asInstanceOf[Double])
      LabeledPoint(row(0).asInstanceOf[Double], Vectors.dense(features))
    }

    val submissionPrediction = submissionLabeled.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (label.toInt, prediction.toInt)
    }
       
    submissionPrediction.saveAsTextFile("/kaggle/titanic/underfit_output")
  }
}