package com.vishnu.spark.kaggle.titanic

import scala.reflect.runtime.universe

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.Bucketizer
import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.mean


object KaggleTitanic {
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("Titanic").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    
    var train_data = load("/kaggle/titanic/train.csv",
      sqlContext,
      "PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked").cache()
      
    var avgAge = train_data.select(mean("Age")).first()(0).asInstanceOf[Double]
    train_data = train_data.na.fill(avgAge, Seq("Age"))
    
    val addChild = sqlContext.udf.register("addChild", (sex: String, age: Double) => {
      if (age < 15)
        "Child"
      else
        sex
    })
    val withFamily = sqlContext.udf.register("withFamily", (sib: Int, par: Int) => {
      if (sib + par > 3)
        1.0
      else
        0.0
    })
    val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))
    val findTitle = sqlContext.udf.register("findTitle", (name: String) => {
      val pattern = "(Dr|Mrs?|Ms|Miss|Master|Rev|Capt|Mlle|Col|Major|Sir|Lady|Mme|Don)\\.".r
      val matchedStr = pattern.findFirstIn(name)  
      var title = matchedStr match {
        case Some(s) => matchedStr.getOrElse("Other.")
        case None => "Other."
      }
      if (title.equals("Don.") || title.equals("Major.") || title.equals("Capt."))
        title = "Sir."
      if (title.equals("Mlle.") || title.equals("Mme."))
          title = "Miss."
      title 
    })
    
    
    train_data = train_data.withColumn("Sex", addChild(train_data("Sex"), train_data("Age")))
    train_data = train_data.withColumn("Title", findTitle(train_data("Name")))
    train_data = train_data.withColumn("Pclass", toDouble(train_data("Pclass")))
    train_data = train_data.withColumn("Family", withFamily(train_data("SibSp"), train_data("Parch")))    
    train_data = train_data.withColumn("Survived", toDouble(train_data("Survived")))
    
    val sexInd = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex")
    val titleInd = new StringIndexer().setInputCol("Title").setOutputCol("TitleIndex")
    //bucketing
    val fareSplits = Array(0.0,10.0,20.0,30.0,40.0,Double.PositiveInfinity)
    val fareBucketize = new Bucketizer().setInputCol("Fare").setOutputCol("FareBucketed").setSplits(fareSplits)
    val assembler = new VectorAssembler().setInputCols(Array("SexIndex", "Age", "TitleIndex", "Pclass", "Family","FareBucketed")).setOutputCol("features_temp")
    val normalizer = new Normalizer().setInputCol("features_temp").setOutputCol("features").setP(1.0)
    val lr = new LogisticRegression().setMaxIter(10)
    lr.setLabelCol("Survived")
    val pipeline = new Pipeline().setStages(Array(sexInd, titleInd, fareBucketize, assembler, normalizer,lr))
    
    val splits = train_data.randomSplit(Array(0.8, 0.2), seed = 11L)
    val train = splits(0).cache()
    val test = splits(1).cache()
    
    var model = pipeline.fit(train)
    var result = model.transform(test)
    result = result.select("prediction","Survived")
    val predictionAndLabels = result.map { row =>
      (row.get(0).asInstanceOf[Double],row.get(1).asInstanceOf[Double])
    }
    
    val metrics = new BinaryClassificationMetrics(predictionAndLabels)
    println("Area under ROC = " + metrics.areaUnderROC())
    
    model = pipeline.fit(train_data)
    
    /**
     * Load the test data, and do all the pre-processing and feature engineering, fit the model.
     */
    var submission_data = load("/kaggle/titanic/test.csv",
      sqlContext,
      "PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked").cache()
    avgAge = submission_data.select(mean("Age")).first()(0).asInstanceOf[Double]
    submission_data = submission_data.na.fill(avgAge, Seq("Age"))
    
    submission_data = submission_data.withColumn("Sex", addChild(submission_data("Sex"), submission_data("Age")))
    submission_data = submission_data.withColumn("Title", findTitle(submission_data("Name")))
    submission_data = submission_data.withColumn("Pclass", toDouble(submission_data("Pclass")))
    submission_data = submission_data.withColumn("Family", withFamily(submission_data("SibSp"), submission_data("Parch")))

    //add column `Survived`
    val getZero = sqlContext.udf.register("toDouble", ((n: Int) => { 0.0 }))
    submission_data = submission_data.withColumn("Survived", toDouble(submission_data("PassengerId")))
    
    result = model.transform(submission_data)
    result = result.select("PassengerId","prediction")
    val submitRDD = result.map { row =>
      (row.get(0).asInstanceOf[Int],row.get(1).asInstanceOf[Double].toInt)
    }
    
    /**
     * Save the RDD for submission
     */
    submitRDD.saveAsTextFile("/kaggle/titanic/output")
  }
  
  def load(path: String, sqlContext: SQLContext, featuresArr: String*): DataFrame = {
    var data = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
      .toDF(featuresArr: _*)
    return data
  }
}