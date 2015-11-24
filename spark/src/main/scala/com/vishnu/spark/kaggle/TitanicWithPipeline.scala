package com.vishnu.spark.kaggle

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.Binarizer
import org.apache.spark.ml.feature.Bucketizer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.functions._
import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.classification.LogisticRegressionModel

object TitanicWithPipeline {

  //TO-DO: Check vector assembler,(may be define a function to do the merge instead of vector assembler) 
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
      "PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked").cache()

    //withFamily

    train_data = preprocess(train_data, sqlContext, true)
    submission_data = preprocess(submission_data, sqlContext, false)
    train_data.show()
    
    
    /*
     *  INDEXING AND VECTOR FORMATION
     */
    val pcInd = new StringIndexer().setInputCol("Pclass").setOutputCol("PclassIndex")
    //val pcEnc = new OneHotEncoder().setInputCol("PclassIndex").setOutputCol("pc")
    val sexInd = new StringIndexer().setInputCol("sexMod").setOutputCol("SexIndex")
    //val sexEnc = new OneHotEncoder().setInputCol("SexIndex").setOutputCol("sx")
    val titleInd = new StringIndexer().setInputCol("Title").setOutputCol("TitleIndex")
    //val titleEnc = new OneHotEncoder().setInputCol("TitleIndex").setOutputCol("title")
    //val fareSplit = Array(0.0, 100.0,200.0,300.0,400.0,500.0)
    //val fareBuc = new Bucketizer().setInputCol("features").setOutputCol("bucketedFeatures").setSplits(fareSplit)
    val assembler = new VectorAssembler().setInputCols(Array("PclassIndex", "SexIndex", "Age", "Fare", "withFamily", "TitleIndex")).setOutputCol("features_temp")
    val normalizer = new Normalizer().setInputCol("features_temp").setOutputCol("features").setP(1.0)
    //do PCA?
    //val pca = new PCA().setInputCol("normFeatures").setOutputCol("pcaFeatures").setK(4)
    val pipeline = new Pipeline().setStages(Array(pcInd, sexInd, titleInd, assembler, normalizer))
    //val model = trainPipeline.fit(train_data)
    var inputTrain = pipeline.fit(train_data).transform(train_data)
    inputTrain = inputTrain.select("label", "features")
    inputTrain.show()
    
    

    /**
     *  NOT USING CROSSVALIDATION
     */
    
    var inputLP = inputTrain.map { row: Row =>
      LabeledPoint(row(0).asInstanceOf[Double], row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
    }
    var model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(inputLP)

    //val testPipeline = new Pipeline().setStages(Array(pcInd, pcEnc, sexInd, sexEnc, titleInd, titleEnc, assembler, normalizer))
    var inputSubmission = pipeline.fit(submission_data).transform(submission_data)
    inputSubmission = inputSubmission.select("label", "features")
    var submissionLP = inputSubmission.map { row: Row =>
      LabeledPoint(row(0).asInstanceOf[Double], row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
    }
    
  
    saveForSubmit(submissionLP, model, "/kaggle/titanic/output", sc)
    
    
    /**
     *  END SUBMISSION
     */
    
    /**
     * CROSS VALIDATION
     */
    /*
    val lr = new LogisticRegression().setMaxIter(10)
    val lrPipe = new Pipeline().setStages(Array(lr))
    val crossval = new CrossValidator().setEstimator(lrPipe)
    val paramGrid = new ParamGridBuilder().build()
    //"f1", "precision", "recall", "weightedPrecision", "weightedRecall"
    val evaluator = new BinaryClassificationEvaluator().setLabelCol("label")
    crossval.setEstimatorParamMaps(paramGrid)
    crossval.setNumFolds(10)
    crossval.setEvaluator(evaluator)
    
    val cvModel = crossval.fit(inputTrain)
    val lrModel = cvModel.bestModel
    
    
    //val labelSub = new StringIndexer().setInputCol("PassengerId").setOutputCol("label")
    val testPipeline = new Pipeline().setStages(Array(pcInd,sexInd,titleInd,assembler,normalizer))
    var inputSubmission = testPipeline.fit(submission_data).transform(submission_data)
    inputSubmission = inputSubmission.select("label","features")
    inputSubmission.show()
    var result = lrModel.transform(inputSubmission)
    
    */
    /**
     *  CROSS VALIDATION END
     */
    

  }

  def preprocess(data: DataFrame, sqlContext: SQLContext, train: Boolean): DataFrame = {
    var train_data = data

    var avgAge = train_data.select(mean("Age")).first()(0).asInstanceOf[Double]
    train_data = train_data.na.fill(avgAge, Seq("Age"))

    var avgFare = train_data.select(mean("Fare")).first()(0).asInstanceOf[Double]
    train_data = train_data.na.fill(avgFare, Seq("Fare"))

    val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))
    val toInt = sqlContext.udf.register("toInt", ((n: Double) => { n.toInt }))
    val withFamily = sqlContext.udf.register("withFamily", (sib: Int, par: Int) => {
      if (sib + par > 3)
        1.0
      else
        0.0
    })

    val findTitle = sqlContext.udf.register("findTitle", (name: String) => {
      val pattern = "(Dr|Mrs?|Ms|Miss|Master|Rev|Capt|Mlle)\\.".r
      val matchedStr = pattern.findFirstIn(name)
      matchedStr match {
        case Some(s) => matchedStr.getOrElse("")
        case None => "Mr"
      }
    })

    val addChild = sqlContext.udf.register("addChild", (sex: String, age: Double) => {
      if (age < 15)
        "Child"
      else
        sex
    })
    if (train)
      train_data = train_data.withColumn("label", toDouble(train_data("Survived")))
    else
      train_data = train_data.withColumn("label", toDouble(train_data("PassengerId")))

    train_data = train_data.withColumn("withFamily", withFamily(train_data("SibSp"), train_data("Parch")))
    train_data = train_data.withColumn("sexMod", addChild(train_data("Sex"), train_data("Age")))
    train_data.withColumn("Title", findTitle(train_data("Name")))
  }

  def load(path: String, sqlContext: SQLContext, featuresArr: String*): DataFrame = {
    var data = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
      .toDF(featuresArr: _*)
    return data
  }

  def saveForSubmit(input: RDD[LabeledPoint], model: LogisticRegressionModel, outputPath: String, sc: SparkContext) {
    //println("PassengerId,Survived")
    val submissionPrediction = input.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (label.toInt, prediction.toInt)
    }
    submissionPrediction.saveAsTextFile(outputPath)
    //submissionPrediction.saveAsTextFile("/kaggle/titanic/output")
  }
}