package com.vishnu.spark.kaggle.titanic

import scala.reflect.runtime.universe
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}
import org.apache.spark.sql.functions.mean
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.tree.configuration.Strategy
import org.apache.spark.ml.feature.PCA
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.ml.feature.RFormula
import org.apache.spark.sql.types._

object TitanicWithPipeline {

  def main(args: Array[String]) {

    val spark = SparkSession.
      builder().
      appName("Titanic").
      master("spark://Vishnus-MacBook-Pro.local:7077").
      getOrCreate()

    val sc = spark.sparkContext
    val sqlContext = spark.sqlContext

    import spark.implicits._

    //1. Load data
    var train_data = load("/kaggle/titanic/train.csv",
      sqlContext,
      "PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked").cache()
    var submission_data = load("/kaggle/titanic/test.csv",
      sqlContext,
      "PassengerId", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked").cache()

    //2. Preprocess data
    train_data = preprocess(train_data, sqlContext, true)
    submission_data = preprocess(submission_data, sqlContext, false)
    train_data.show()

    /**
     *  INDEXING AND VECTOR FORMATION
     */
    val sexInd = new StringIndexer().setInputCol("sexMod").setOutputCol("SexIndex")
    val titleInd = new StringIndexer().setInputCol("Title").setOutputCol("TitleIndex")
    //val titleEnc = new OneHotEncoder().setInputCol("title_temp").setOutputCol("TitleIndex")
    val fareInd = new StringIndexer().setInputCol("Fare").setOutputCol("FareIndex")
    //val fareSplit = Array(0.0,10.0,20.0,30.0,40.0,50.0,60.0,70.0,80.0,100.0,130.0,160.0,190.0,240.0,290.0,390.0,490.0,590.0);
    //val fareBuc = new Bucketizer().setInputCol("Fare").setOutputCol("FareIndex").setSplits(fareSplit)
    val assembler = new VectorAssembler().setInputCols(Array("Pclass", "SexIndex", "Age", "FareIndex", "withFamily", "TitleIndex")).setOutputCol("features_temp")
    val normalizer = new Normalizer().setInputCol("features_temp").setOutputCol("features").setP(1.0)
    //adding PCA reduced score from 78 to 68
    //val pca = new PCA().setInputCol("normFeatures").setOutputCol("features").setK(4)
    val pipeline = new Pipeline().setStages(Array(sexInd, titleInd, fareInd, assembler, normalizer))
    //val model = trainPipeline.fit(train_data)
    var inputTrain = pipeline.fit(train_data).transform(train_data)
    //val formula = new RFormula().setFormula("prediction ~ SexIndex+TitleIndex+FareIndex").setFeaturesCol("features").setLabelCol("label")
    //inputTrain = formula.fit(inputTrain).transform(inputTrain)
    inputTrain = inputTrain.select("Survived", "features")
    inputTrain.show()
    /**
     * CROSS VALIDATION
     */

    //LOGISTIC REGRESSION
    
    val lr = new LogisticRegression().setMaxIter(10)
    lr.setLabelCol("Survived")
    val lrPipe = new Pipeline().setStages(Array(lr))
    val bEval = new BinaryClassificationEvaluator().setLabelCol("Survived")
    val crossval = new CrossValidator().setEstimator(lrPipe)
    crossval.setEvaluator(bEval)
    
    
    //RANDOM FOREST
    /*
    val labelInd = new StringIndexer().setInputCol("label").setOutputCol("labelInd")
    val rf = new RandomForestClassifier().setLabelCol("labelInd").setFeaturesCol("features").setNumTrees(64)
    val rfPipe = new Pipeline().setStages(Array(labelInd, rf))
    val mclassEval = new MulticlassClassificationEvaluator().setLabelCol("labelInd").setPredictionCol("prediction").setMetricName("precision")
    val crossval = new CrossValidator().setEstimator(rfPipe)
    crossval.setEvaluator(mclassEval)
    */
    
    val paramGrid = new ParamGridBuilder().build()
    crossval.setEstimatorParamMaps(paramGrid)
    crossval.setNumFolds(10)

    val cvModel = crossval.fit(inputTrain)
    val lrModel = cvModel.bestModel

    //val labelSub = new StringIndexer().setInputCol("PassengerId").setOutputCol("label")
    //val testPipeline = new Pipeline().setStages(Array(pcInd,sexInd,titleInd,assembler,normalizer))
    var inputSubmission = pipeline.fit(submission_data).transform(submission_data)
    //inputSubmission = formula.fit(inputSubmission).transform(inputSubmission)
    inputSubmission = inputSubmission.select("Survived", "features", "pid")
    inputSubmission.show()
    var result = lrModel.transform(inputSubmission)
    result.show();
    var saveDf = result.select("pid", "prediction")
    var saveDataset = saveDf.map(r => (r.get(0).asInstanceOf[Double].toInt, r.get(1).asInstanceOf[Double].toInt))
    saveDataset.rdd.saveAsTextFile("/kaggle/titanic/output")
    /**
     *  CROSS VALIDATION END
     */

    /**
     *  WITH-OUT USING CROSS VALIDATION
     */

    /*
    var inputLP = inputTrain.map { row: Row =>
      LabeledPoint(row(0).asInstanceOf[Double], row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
    }
    var model = new LogisticRegressionWithLBFGS().setNumClasses(2).run(inputLP)

    //val testPipeline = new Pipeline().setStages(Array(pcInd, pcEnc, sexInd, sexEnc, titleInd, titleEnc, assembler, normalizer))
    var inputSubmission = pipeline.fit(submission_data).transform(submission_data)
    //inputSubmission.show();
    inputSubmission = inputSubmission.select("label", "features")
    var submissionLP = inputSubmission.map { row: Row =>
      LabeledPoint(row(0).asInstanceOf[Double], row.getAs[org.apache.spark.mllib.linalg.SparseVector]("features"))
    }
  
    saveForSubmit(submissionLP, model, "/kaggle/titanic/output", sc)
    */

    /**
     *  END SUBMISSION
     */

  }

  /**
   * Pre-process data,
   * 	a) Fill null values
   * 	b) Transform Int columns to Double
   *  c) Derive new features from existing ones
   *  d) Create a label column(needed for some of the models)
   */
  def preprocess(data: DataFrame, sqlContext: SQLContext, train: Boolean): DataFrame = {
    var train_data = data

    //Fill missing age with average (age)
    var avgAge = train_data.select(mean("Age")).first()(0).asInstanceOf[Double]
    train_data = train_data.na.fill(avgAge, Seq("Age"))

    //Fill missing fares with average(fare)
    var avgFare = train_data.select(mean("Fare")).first()(0).asInstanceOf[Double]
    train_data = train_data.na.fill(avgFare, Seq("Fare"))

    //withFamily is true(1) if the family size excluding self is > 3 (large family may have more/less chance of survival)
    val withFamily = sqlContext.udf.register("withFamily", (sib: Int, par: Int) => {
      if (sib + par > 3)
        1.0
      else
        0.0
    })

    //extract title information from the Name field
    val findTitle = sqlContext.udf.register("findTitle", (name: String) => {
      val pattern = "(Dr|Mrs?|Ms|Miss|Master|Rev|Capt|Mlle)\\.".r
      val matchedStr = pattern.findFirstIn(name)
      matchedStr match {
        case Some(s) => matchedStr.getOrElse("")
        case None => "Mr"
      }
    })

    //Categorize a passenger as child if his/her age is less than 15 (more chances of survival)
    val addChild = sqlContext.udf.register("addChild", (sex: String, age: Double) => {
      if (age < 15)
        "Child"
      else
        sex
    })

    //convert Pclass to double
    val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))
    train_data = train_data.withColumn("Pclass", toDouble(train_data("Pclass")))
    if (train) {
      //create label field, needed for prediction
      train_data = train_data.withColumn("Survived", toDouble(train_data("Survived")))
    } else {
      //The classifier needs a label column which is not present in the test data, so generating a dummy one
      val getZero = sqlContext.udf.register("toDouble", ((n: Int) => { 0.0 }))
      train_data = train_data.withColumn("Survived", getZero(train_data("PassengerId")))
      //passenger id for which prediction is going to be made, 
      //Prediction is based no `features` column but we need this to map the predicted value with passenger
      train_data = train_data.withColumn("pid", toDouble(train_data("PassengerId")))
    }

    train_data = train_data.withColumn("withFamily", withFamily(train_data("SibSp"), train_data("Parch")))
    train_data = train_data.withColumn("sexMod", addChild(train_data("Sex"), train_data("Age")))
    train_data.withColumn("Title", findTitle(train_data("Name")))
  }

  /**
   * load method: loads an input csv file using spark-csv and returns a DataFrame object
   */
  def load(path: String, sqlContext: SQLContext, featuresArr: String*): DataFrame = {
    var data = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
      .toDF(featuresArr: _*)
    return data
  }

  /**
   * This method is used for predicting and saving result for submitting on to kaggle.
   * It takes in an RDD, which will have the label = PassengerId, and features. The features are passed on to the model
   * to get the prediction.
   * An RDD of passengerId:Int,prediction:Int is returned.
   *
   */
  def saveForSubmit(input: RDD[LabeledPoint], model: LogisticRegressionModel, outputPath: String, sc: SparkContext) {
    val submissionPrediction = input.map {
      case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (label.toInt, prediction.toInt)
    }
    submissionPrediction.saveAsTextFile(outputPath)
  }
}