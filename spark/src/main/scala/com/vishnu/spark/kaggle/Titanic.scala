package com.vishnu.spark.kaggle

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.functions._
import org.apache.spark.ml.feature.Normalizer
import scala.util.matching.Regex
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler

/**
 * @author vishnu viswanath
 */
object Titanic {

  /**
   * Stages:
   * 1. Collect data
   * 2. Pre-process data
   * 		a) Data cleansing
   * 		b) Transformation
   * 3. Build Model
   * 4. Evaluate model
   */
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("Titanic").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    //1. Load data
    var train_data = load("/kaggle/titanic/train.csv",
      sqlContext,
      "PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")
    var submission_data = load("/kaggle/titanic/test.csv",
      sqlContext,
      "PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")

    //2. Pre-process data
    var processed_data = preprocess(train_data, sqlContext, true)
    
    //3. Build model
    val model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(processed_data)

    //4. Preprocess submission data
    var processed_submission = preprocess(submission_data, sqlContext, false)
    
    //5. Evalaute
    val submissionPrediction = processed_submission.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (label.toInt, prediction.toInt)
    }
    
    submissionPrediction.saveAsTextFile("/kaggle/titanic/output")
    
  }
  
  
  
  

  def load(path: String, sqlContext: SQLContext, featuresArr: String*): DataFrame = {
    var data = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
      .toDF(featuresArr: _*)
    return data
  }
  
  
  

  def preprocess(data: DataFrame, sqlContext: SQLContext, train: Boolean): RDD[LabeledPoint] = {
    //1. deal with missing value
    //Missing ages can be filled by avg(age)
    var avgAge = data.select(mean("Age")).first()(0).asInstanceOf[Double]
    var prepared = data.na.fill(avgAge, Seq("Age"))

    //2. Discover/Derive new features
    val hasFamily = sqlContext.udf.register("HasFamily", (siblings: Int, parents: Int) => {
      var count = siblings + parents
      if (count > 0)
        1.0
      else
        0.0
    })
    
    def findStatus(name: String) = {
      val married = "Mr(s).".r
      val matchedStr = married.findFirstIn(name)
      matchedStr match {
        case Some(s) => "Married"
        case None => "Unmarried"
      }
    }
    //we can discover if a person is married or not from the title in name.
    val findMaritalStatus = sqlContext.udf.register("findStatus",findStatus _)
    /*
    val findMaritalStatus = sqlContext.udf.register("findMaritalStatus", (name: String) => {
      /*
      val married = "Mr(s).".r
      val matchedStr = married.findFirstIn(name)
      matchedStr match {
        case Some(s) => "Married"
        case None => "Unmarried"
      }*/
      1.0
    })*/
    prepared = prepared.withColumn("Status", findMaritalStatus(prepared("Name")))

    
    prepared = prepared.withColumn("HasFamily", hasFamily(prepared("SibSp"), prepared("Parch")))

    //3. Check correlation

    //4. transform to double
    var indexer = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex").fit(prepared)
    prepared = indexer.transform(prepared)
    indexer = new StringIndexer().setInputCol("Cabin").setOutputCol("CabinIndex").fit(prepared)
    prepared = indexer.transform(prepared)
    indexer = new StringIndexer().setInputCol("Embarked").setOutputCol("EmbarkedIndex").fit(prepared)
    prepared = indexer.transform(prepared)
    indexer = new StringIndexer().setInputCol("Status").setOutputCol("StatusIndex").fit(prepared)
    prepared = indexer.transform(prepared)

    //5. Drop unnecessary features
    prepared = prepared.drop("Name")
      .drop("SibSp")
      .drop("Parch")
      .drop("Ticket")
      .drop("Fare")
      .drop("Sex")
      .drop("Cabin")
      .drop("Embarked")
      .drop("Status")
    if (train)
      prepared = prepared.drop("PassengerId")

    //Transform columns into double
    val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))
    if (train) {
      prepared = prepared.withColumn("Survived", toDouble(prepared("Survived"))).withColumn("Pclass", toDouble(prepared("Pclass")))
    }
    else
      prepared = prepared.withColumn("PassengerId", toDouble(prepared("PassengerId"))).withColumn("Pclass", toDouble(prepared("Pclass")))

    //convert DF to RDD[labeledPoints]
    prepared.show()
    
    //assembler did not produce proper vector
    //var assembler = new VectorAssembler().setInputCols(Array("Pclass", "Age", "HasFamily","SexIndex","CabinIndex","EmbarkedIndex","StatusIndex")).setOutputCol("features")
    //prepared = assembler.transform(prepared)
    
    
    val toVector  = sqlContext.udf.register("toVector", (a:Double,b:Double,c:Double,d:Double,e:Double,f:Double) => {Vectors.dense(a,b,c,d,e,f)})
    prepared = prepared.withColumn("features",toVector(prepared("Pclass"),prepared("Age"),prepared("HasFamily"),prepared("SexIndex"),prepared("EmbarkedIndex"),prepared("StatusIndex")))
    prepared = prepared.drop("Pclass").drop("Age").drop("HasFamily").drop("SexIndex").drop("CabinIndex").drop("EmbarkedIndex").drop("StatusIndex")
    prepared.show()
    
    val normalizer = new Normalizer().setInputCol("features").setOutputCol("normFeatures").setP(1.0)
    prepared = normalizer.transform(prepared)  
    prepared = prepared.drop("features")
    prepared = prepared.withColumnRenamed("normFeatures","features")
    if (train) {
      /*
      var labeledPoints = prepared.map { row: Row =>
        val features = Array[Double](row(1).asInstanceOf[Double],
          row(2).asInstanceOf[Double],
          row(3).asInstanceOf[Double],
          row(4).asInstanceOf[Double],
          row(5).asInstanceOf[Double],
          row(6).asInstanceOf[Double],
          row(7).asInstanceOf[Double])
        LabeledPoint(row(0).asInstanceOf[Double], Vectors.dense(features))
      }
      */
      
      var labeledPoints = prepared.map { row: Row =>
        LabeledPoint(row(0).asInstanceOf[Double],row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
      }
      
        
      return labeledPoints
    } else {
      var labeledPoints = prepared.map { row: Row =>
        LabeledPoint(row(0).asInstanceOf[Double],row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
      }
      return labeledPoints
    }

    //5. normalize
    //6. Dimensionality reduction
    
  }

  def findStatus1(name: String): String = {
    val married = "Mr(s).".r
    val matchedStr = married.findFirstIn(name)
    matchedStr match {
      case Some(s) => return "Married"
      case None => return "Unmarried"
    }
  }
}