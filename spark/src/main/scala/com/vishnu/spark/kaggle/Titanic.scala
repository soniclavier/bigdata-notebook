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
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.classification.LogisticRegressionModel
import com.vishnu.spark.Evaluator
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

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
      "PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked").cache()
    var submission_data = load("/kaggle/titanic/test.csv",
      sqlContext,
      "PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked")

    //Pre-process data
    //val splits = train_data.randomSplit(Array(0.8, 0.2), seed = 11L)
    //val training = splits(0).cache()
    //val test = splits(1)
    var processed_full = preprocess(train_data, sqlContext, true)
    //var processed_train = preprocess(training, sqlContext, true)
    //var processed_test = preprocess(test, sqlContext, true)
    
    //Build model
    val model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(processed_full)

    //val categoricalFeaturesInfo = Map[Int, Int]()
    //val model = RandomForest.trainClassifier(processed_full,2, categoricalFeaturesInfo, 25, "auto", "gini", 4, 32)    
    //val model = DecisionTree.trainClassifier(processed_full,2, categoricalFeaturesInfo, "gini", 5, 32)

    //Evalaute
    /*
    val predictionAndLabels = processed_test.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (prediction, label)
    }
    Evaluator.evaluate(predictionAndLabels, "LR")
	*/
    //Preprocess submission data
    var processed_submission = preprocess(submission_data, sqlContext, false)

    //Save output
    saveForSubmit(processed_submission,model,"/kaggle/titanic/output",sc)

  }

  
  def saveForSubmit(input: RDD[LabeledPoint], model: LogisticRegressionModel, outputPath: String, sc: SparkContext) {
    println("PassengerId,Survived")
    val submissionPrediction = input.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        println(label.toInt+","+prediction.toInt)
        (label.toInt, prediction.toInt)
    }
    submissionPrediction.saveAsTextFile(outputPath)
    //submissionPrediction.saveAsTextFile("/kaggle/titanic/output")
  }
  
  def saveForSubmit(input: RDD[LabeledPoint], model: RandomForestModel, outputPath: String, sc: SparkContext) {
    println("PassengerId,Survived")
    val submissionPrediction = input.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        println(label.toInt+","+prediction.toInt)
        (label.toInt, prediction.toInt)
    }
    submissionPrediction.saveAsTextFile(outputPath)
    //submissionPrediction.saveAsTextFile("/kaggle/titanic/output")
  }
  
  def saveForSubmit(input: RDD[LabeledPoint], model: DecisionTreeModel, outputPath: String, sc: SparkContext) {
    println("PassengerId,Survived")
    val submissionPrediction = input.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        println(label.toInt+","+prediction.toInt)
        (label.toInt, prediction.toInt)
    }
    submissionPrediction.saveAsTextFile(outputPath)
    //submissionPrediction.saveAsTextFile("/kaggle/titanic/output")
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
    var prepared = data
    //if(train)
    //  prepared = data.na.drop()

    var avgAge = data.select(mean("Age")).first()(0).asInstanceOf[Double]
    prepared = prepared.na.fill(avgAge, Seq("Age"))

    var avgFare = data.select(mean("Fare")).first()(0).asInstanceOf[Double]
    prepared = prepared.na.fill(avgFare, Seq("Fare"))
    //prepared = prepared.na.fill("S", Seq("Embarked"))

    //2. Discover/Derive new features
    val familyCount = sqlContext.udf.register("familyCount", (siblings: Int, parents: Int) => {
      (siblings + parents + 1).toDouble
    })
    
    val withFamily = sqlContext.udf.register("withFamily",(sib: Int,par: Int) => {
      if (sib+par>3)
        1.0
      else
        0.0
    })

    val transformSex = sqlContext.udf.register("transformSex", (sex: String,age: Double) => {
      if (age <15)
        "Child"
      else
        sex
    })
    
    val findTitle = sqlContext.udf.register("findTitle", (name: String) => {
      val pattern = "(Dr|Mrs?|Ms|Miss|Master|Rev|Capt|Mlle)\\.".r
      val matchedStr = pattern.findFirstIn(name)
      matchedStr match {
        case Some(s) => matchedStr.getOrElse("")
        case None => "Mr"
      }
    })
    
    val getDeck = sqlContext.udf.register("getDeck",(cabin: String) => {
      var deck = cabin.trim()
      if (deck.length() != 0)
        deck.substring(0,1)
      else
        "UD"
    })
    prepared = prepared.withColumn("Title", findTitle(prepared("Name")))
    prepared = prepared.withColumn("Sex", transformSex(prepared("Sex"),prepared("Age")))
    prepared = prepared.withColumn("WithFamily", withFamily(prepared("SibSp"),prepared("Parch")))
    //prepared = prepared.withColumn("Deck", getDeck(prepared("Cabin")))
    //prepared = prepared.withColumn("FamilyMemberCount", familyCount(prepared("SibSp"), prepared("Parch")))

    //3. Check correlation

    //4. transform to double
    var indexer = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex").fit(prepared)
    prepared = indexer.transform(prepared)
    //indexer = new StringIndexer().setInputCol("Cabin").setOutputCol("CabinIndex").fit(prepared)
    //prepared = indexer.transform(prepared)
    //indexer = new StringIndexer().setInputCol("Embarked").setOutputCol("EmbarkedIndex").fit(prepared)
    //prepared = indexer.transform(prepared)
    indexer = new StringIndexer().setInputCol("Title").setOutputCol("TitleIndex").fit(prepared)
    prepared = indexer.transform(prepared)
    indexer = new StringIndexer().setInputCol("Fare").setOutputCol("FareIndex").fit(prepared)
    prepared = indexer.transform(prepared)
    //indexer = new StringIndexer().setInputCol("Ticket").setOutputCol("TicketIndex").fit(prepared)
    //prepared = indexer.transform(prepared)
    //indexer = new StringIndexer().setInputCol("Deck").setOutputCol("DeckIndex").fit(prepared)
    //prepared = indexer.transform(prepared)

    //5. Drop unnecessary features
    prepared = prepared.drop("Name")
      .drop("SibSp")
      .drop("Parch")
      .drop("Ticket")
      .drop("Fare")
      .drop("Sex")
      .drop("Cabin")
      .drop("Embarked")
      .drop("Title")
      .drop("Deck")
    if (train)
      prepared = prepared.drop("PassengerId")

    //Transform columns into double
    val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))
    if (train) {
      prepared = prepared.withColumn("Survived", toDouble(prepared("Survived"))).withColumn("Pclass", toDouble(prepared("Pclass")))
    } else
      prepared = prepared.withColumn("PassengerId", toDouble(prepared("PassengerId"))).withColumn("Pclass", toDouble(prepared("Pclass")))

    //convert DF to RDD[labeledPoints]

    //assembler did not produce proper vector
    //var assembler = new VectorAssembler().setInputCols(Array("Pclass", "Age", "HasFamily","SexIndex","CabinIndex","EmbarkedIndex","TitleIndex")).setOutputCol("features")
    //prepared = assembler.transform(prepared)
    prepared.show()
    val toVector = sqlContext.udf.register("toVector", (a: Double, b: Double, c: Double, d: Double, e:Double,f:Double) => { Vectors.dense(a, b, c, d, e,f) })
    prepared = prepared.withColumn("features", toVector(prepared("Pclass"), prepared("Age"),prepared("TitleIndex"),prepared("SexIndex"),prepared("FareIndex"),prepared("WithFamily")))
    prepared = prepared.drop("Pclass").drop("Age").drop("FareIndex").drop("TitleIndex").drop("SexIndex").drop("WithFamily")
    prepared.show()

    //val normalizer = new Normalizer().setInputCol("features").setOutputCol("normFeatures").setP(1.0)
    //prepared = normalizer.transform(prepared)
    //prepared = prepared.drop("features")
    //prepared = prepared.withColumnRenamed("normFeatures", "features")
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
        LabeledPoint(row(0).asInstanceOf[Double], row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
      }

      return labeledPoints
    } else {
      var labeledPoints = prepared.map { row: Row =>
        LabeledPoint(row(0).asInstanceOf[Double], row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
      }
      return labeledPoints
    }
    
   

    //5. normalize
    //6. Dimensionality reduction

  }

}