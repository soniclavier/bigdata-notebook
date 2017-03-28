---
layout: post
comments: true
title: Building Classification model using Apache Spark
date: 2015-11-25
PAGE_IDENTIFIER: spark_ml
permalink: /spark_lr.html
tags: ApacheSpark MachineLearning Classification Scala BigData Hadoop
description: Build a LogisticRegression classification model to predict survival of passengers in Titanic disaster. The blog tries to solve the Kaggle knowledge challenge - Titanic Machine Learning from Disaster using Apache Spark and Scala.
---
<div class="col three">
	<img class="col three" src="/img/spark_ml_logo.png">
</div>
The aim of this blog is to explain how to use SparkML to build a Classification model. To explain the usage better, I am going to try solve the Kaggle knowledge challenge - [Titanic: Machine Learning from Disaster](https://www.kaggle.com/c/titanic). The source code of this project is available in my [github](https://github.com/soniclavier/hadoop/tree/master/spark/src/main/scala/com/vishnu/spark/kaggle/titanic/KaggleTitanic.scala)

In this challenge, we are given a set of details of passengers such as name, gender, fare, cabin etc and if the person survived the Titanic disaster. Based on this we have to build a Model that can predict, given another passenger, if he/she is likely to survive. This is an example of binary classification where there are only two possible classes(1 if passenger survives and 0 if not).

- The first step when trying to build a machine learning model is to analyze and understand the data you have. So that you can decide which all features has to be used for building the model, whether the features are numeric or categorical, what is the mean,max or min of your numerical features and so on. 
- Once the data is analyzed, next step is feature selection where we decide which all features are relevant for building the model 
- Next is data preprocessing. The input data that you receive for modeling is not going to be good data most of the times. During this stage, for example, we can decide on what to do with the missing values - whether to drop rows having nulls, or fill those with average value of the feature(if feature is numerical), or fill with most occurring value of the feature(if feature is categorical) etc. 
- Next comes the Feature engineering and Feature transformation step. In Feature engineering we derive new features from existing ones and during feature transformation we transform existing features so that it can be used for building the model.
- Finally we build the model using the selected features and do prediction on a new set of data.

We will be implementing all of the above steps using Spark and Scala and will be building a machine learning pipeline - the overall flow can be shown by the diagram below. The grey section of the diagram shows the model building flow and the blue section of the diagram shows the flow for making prediction.

<div class="col three">
	<img class="col three expandable" src="/img/spark_ml_flow.png">
</div>
<br/><br/>

### **Load and Analyze data**
<br/>
As mentioned earlier, first step is to analyze the data. To do that, we have to first load data into `Spark`. Download the train.csv file from [here](https://www.kaggle.com/c/titanic/data), and open the file and check the content

{% highlight sh %}
$ head train.csv
PassengerId,Survived,Pclass,Name,Sex,Age,SibSp,Parch,Ticket,Fare,Cabin,Embarked
1,0,3,"Braund, Mr. Owen Harris",male,22,1,0,A/5 21171,7.25,,S
2,1,1,"Cumings, Mrs. John Bradley (Florence Briggs Thayer)",female,38,1,0,PC 17599,71.2833,C85,C
3,1,3,"Heikkinen, Miss. Laina",female,26,0,0,STON/O2. 3101282,7.925,,S
4,1,1,"Futrelle, Mrs. Jacques Heath (Lily May Peel)",female,35,1,0,113803,53.1,C123,S
5,0,3,"Allen, Mr. William Henry",male,35,0,0,373450,8.05,,S
6,0,3,"Moran, Mr. James",male,,0,0,330877,8.4583,,Q
7,0,1,"McCarthy, Mr. Timothy J",male,54,0,0,17463,51.8625,E46,S
8,0,3,"Palsson, Master. Gosta Leonard",male,2,3,1,349909,21.075,,S
9,1,3,"Johnson, Mrs. Oscar W (Elisabeth Vilhelmina Berg)",female,27,0,2,347742,11.1333,,S
{% endhighlight %}
As you can see, the file contains a header row which has PassengerId, Survived, Pclass, Name, Sex, Age, SibSp ,Parch ,Ticket ,Fare ,Cabin and Embarked. You can find more information about what each of these fields are from the Kaggle website. Move this file to some folder in  HDFS(I have kept mine at `/kaggle/titanic/train.csv`). The data is in csv format, to load csv files we will use the library [spark-csv](https://github.com/databricks/spark-csv).

We will define a simple load function that can be used to load csv file. First start your spark-shell using the below command.
{% highlight sh %}
spark-shell --master spark://yourspark-server-url --packages com.databricks:spark-csv_2.11:1.3.0
{% endhighlight %}
*Note: You will have to import a few classes for this project, which can be found [here](https://github.com/soniclavier/hadoop/tree/master/spark/src/main/scala/com/vishnu/spark/kaggle/titanic/KaggleTitanic.scala)*
{% highlight scala %}
def load(path: String, sqlContext: SQLContext, featuresArr: String*): DataFrame = {
    var data = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
      .toDF(featuresArr: _*)
    return data
  }
{% endhighlight %}
The method takes 3 inputs - the path where the csv file is, sqlContext and a featuresArr which is used to name the columns being loaded. We don't really have to give the featuresArr here since our csv file contains header information. If not, the column names would have been assigned default values such as C0, C1 etc. 

Use the load method defined, to load csv file and create a DataFrame
{% highlight scala %}
var train_data = load("/kaggle/titanic/train.csv",
      sqlContext,
      "PassengerId", "Survived", "Pclass", "Name", "Sex", "Age", "SibSp", "Parch", "Ticket", "Fare", "Cabin", "Embarked"
      ).cache()
{% endhighlight %}
*Note: We are caching the dataFrame in-memory by calling `cache()`, this will help improve the performance during model building.*

Now we will explore the loaded DataFrame for to understand the data better. We can check the schema of the loaded data by calling
{% highlight scala %}
scala> train_data.printSchema()
root
 |-- PassengerId: integer (nullable = true)
 |-- Survived: integer (nullable = true)
 |-- Pclass: integer (nullable = true)
 |-- Name: string (nullable = true)
 |-- Sex: string (nullable = true)
 |-- Age: double (nullable = true)
 |-- SibSp: integer (nullable = true)
 |-- Parch: integer (nullable = true)
 |-- Ticket: string (nullable = true)
 |-- Fare: double (nullable = true)
 |-- Cabin: string (nullable = true)
 |-- Embarked: string (nullable = true)
{% endhighlight %}
As you can see, the spark-csv library has inferred the data type of each column. If you go back and check the load method you can see that we have used, `.option("inferSchema", "true")` which tells the library to do so. If not set, all the fields will set to type `string`.
show() method in DataFrame can be used to display the dataframe in tabular form. You can also pass an int to this method to tell how many rows to be displayed. e.g.,`df.show(100)`
{% highlight scala %}
scala> train_data.show()
+-----------+--------+------+--------------------+------+----+-----+-----+----------------+-------+-----+--------+
|PassengerId|Survived|Pclass|                Name|   Sex| Age|SibSp|Parch|          Ticket|   Fare|Cabin|Embarked|
+-----------+--------+------+--------------------+------+----+-----+-----+----------------+-------+-----+--------+
|          1|       0|     3|Braund, Mr. Owen ...|  male|22.0|    1|    0|       A/5 21171|   7.25|     |       S|
|          2|       1|     1|Cumings, Mrs. Joh...|female|38.0|    1|    0|        PC 17599|71.2833|  C85|       C|
|          3|       1|     3|Heikkinen, Miss. ...|female|26.0|    0|    0|STON/O2. 3101282|  7.925|     |       S|
|          4|       1|     1|Futrelle, Mrs. Ja...|female|35.0|    1|    0|          113803|   53.1| C123|       S|
|          5|       0|     3|Allen, Mr. Willia...|  male|35.0|    0|    0|          373450|   8.05|     |       S|
|          6|       0|     3|    Moran, Mr. James|  male|null|    0|    0|          330877| 8.4583|     |       Q|
|          7|       0|     1|McCarthy, Mr. Tim...|  male|54.0|    0|    0|           17463|51.8625|  E46|       S|
|          8|       0|     3|Palsson, Master. ...|  male| 2.0|    3|    1|          349909| 21.075|     |       S|
|          9|       1|     3|Johnson, Mrs. Osc...|female|27.0|    0|    2|          347742|11.1333|     |       S|
|         10|       1|     2|Nasser, Mrs. Nich...|female|14.0|    1|    0|          237736|30.0708|     |       C|
|         11|       1|     3|Sandstrom, Miss. ...|female| 4.0|    1|    1|         PP 9549|   16.7|   G6|       S|
|         12|       1|     1|Bonnell, Miss. El...|female|58.0|    0|    0|          113783|  26.55| C103|       S|
|         13|       0|     3|Saundercock, Mr. ...|  male|20.0|    0|    0|       A/5. 2151|   8.05|     |       S|
|         14|       0|     3|Andersson, Mr. An...|  male|39.0|    1|    5|          347082| 31.275|     |       S|
|         15|       0|     3|Vestrom, Miss. Hu...|female|14.0|    0|    0|          350406| 7.8542|     |       S|
|         16|       1|     2|Hewlett, Mrs. (Ma...|female|55.0|    0|    0|          248706|   16.0|     |       S|
|         17|       0|     3|Rice, Master. Eugene|  male| 2.0|    4|    1|          382652| 29.125|     |       Q|
|         18|       1|     2|Williams, Mr. Cha...|  male|null|    0|    0|          244373|   13.0|     |       S|
|         19|       0|     3|Vander Planke, Mr...|female|31.0|    1|    0|          345763|   18.0|     |       S|
|         20|       1|     3|Masselmani, Mrs. ...|female|null|    0|    0|            2649|  7.225|     |       C|
+-----------+--------+------+--------------------+------+----+-----+-----+----------------+-------+-----+--------+
only showing top 20 rows
{% endhighlight %}
You can also see stats about any numerical column by using `dataFrame.describe("column")`. e.g.,
{% highlight scala %}
scala> train_data.describe("Fare").show()
+-------+------------------+
|summary|              Fare|
+-------+------------------+
|  count|               891|
|   mean|32.204207968574615|
| stddev|49.665534444774124|
|    min|                 0|
|    max|              93.5|
+-------+------------------+
{% endhighlight %}

Play around with other columns also till you get an idea on how the data is.
<br/>

### **Pre-process**  
<br/>

#### **Fill missing values**
On analyzing the data, you can see a few irregularities in it. For example there are few missing values in column Age. Similarly there are null/missing values in Cabin, Fare and Embarked. There are several techniques for filling in the missing values. You can

- Ignore/drop the rows having missing values. This can be done in spark by calling <br/>
{% highlight scala %}
var train_na_removed = train_data.na.drop()
{% endhighlight %}
- If the column is numerical, fill in the missing value with the mean/avg value of the column. We are going to replace the missing values in Age column by using this method.
{% highlight scala %}
var avgAge = train_data.select(mean("Age")).first()(0).asInstanceOf[Double]
train_data = train_data.na.fill(avgAge, Seq("Age"))

{% endhighlight %}

- If the column is categorical, fill in with the most occurring category <br/>
{% highlight scala %}
//Note: we are not using this feature in our model. Below example is shown to explain how to do this in spark
var train_embarked_filled = train_data.na.fill("S", Seq("Embarked"))
{% endhighlight %}
- Build a machine learning model which can predict those missing values.

#### **Discover new features**
In many cases, there will be features in your input data that can be used to derive new features which will help in building a better model. This is also called `Feature Engineering`. For example, if you take a closer look at the column 'Name', you can see that the format is `FirstName Title. LastName`. We could not possibly make any prediction based on the passenger's name but may be there is some relation between the Title and the passenger's survival. So, we will extract the title from each name and form a new column/feature. The udf `findTitle` is used for extracting title from a given string.
{% highlight scala %}
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
{% endhighlight %}
DataFrame provides a method `withColumn` which can be used for adding/replacing an existing column. It takes two parameters - the name of the new column and a `Column` of the current DataFrame. i.e., if you call 
{% highlight scala %}
var temp  = train_data.withColumn("test",train_data("PassengerId"))
//^will create a new column named test with same values as in the column PassengerId.
//we can also modify the value of the new column. e.g.,
temp = train_data.withColumn("test",train_data("PassengerId")-1)
temp.select("PassengerId","test").show(3)
+-----------+----+
|PassengerId|test|
+-----------+----+
|          1|   0|
|          2|   1|
|          3|   2|
+-----------+----+
{% endhighlight %}
We will now apply the function `findTitle` on the `Name` column to extract title and create a new column - *Title*. 
{% highlight scala %}
train_data = train_data.withColumn("Title", findTitle(train_data("Name")))
train_data.select("Name","Title").show()
+--------------------+-------+
|                Name|  Title|
+--------------------+-------+
|Braund, Mr. Owen ...|    Mr.|
|Cumings, Mrs. Joh...|   Mrs.|
|Heikkinen, Miss. ...|  Miss.|
|Futrelle, Mrs. Ja...|   Mrs.|
|Allen, Mr. Willia...|    Mr.|
|    Moran, Mr. James|    Mr.|
|McCarthy, Mr. Tim...|    Mr.|
|Palsson, Master. ...|Master.|
|Johnson, Mrs. Osc...|   Mrs.|
|Nasser, Mrs. Nich...|   Mrs.|
|Sandstrom, Miss. ...|  Miss.|
|Bonnell, Miss. El...|  Miss.|
|Saundercock, Mr. ...|    Mr.|
|Andersson, Mr. An...|    Mr.|
|Vestrom, Miss. Hu...|  Miss.|
|Hewlett, Mrs. (Ma...|   Mrs.|
|Rice, Master. Eugene|Master.|
|Williams, Mr. Cha...|    Mr.|
|Vander Planke, Mr...|   Mrs.|
|Masselmani, Mrs. ...|   Mrs.|
+--------------------+-------+
only showing top 20 rows
{% endhighlight %}
Similarly we will define 3 other udfs, using which we will generate new features. 
{% highlight scala %}
//Categorize a passenger as child if his/her age is less than 15 
//(more chances of survival)
    val addChild = sqlContext.udf.register("addChild", (sex: String, age: Double) => {
      if (age < 15)
        "Child"
      else
        sex
    })

//withFamily is true(1) if the family size excluding self is > 3 
//(large family may have more/less chance of survival)
    val withFamily = sqlContext.udf.register("withFamily", (sib: Int, par: Int) => {
      if (sib + par > 3)
        1.0
      else
        0.0
    })
//for converting integer columns to double. Requires since few of the 
//columns of our DataFrame are of Int type.
val toDouble = sqlContext.udf.register("toDouble", ((n: Int) => { n.toDouble }))

//apply the udfs
train_data = train_data.withColumn("Sex", addChild(train_data("Sex"), train_data("Age")))
train_data = train_data.withColumn("Pclass", toDouble(train_data("Pclass")))
train_data = train_data.withColumn("Family", withFamily(train_data("SibSp"), train_data("Parch")))    
train_data = train_data.withColumn("Survived", toDouble(train_data("Survived")))
{% endhighlight %}

### **Pipeline Components**
<br/>
ML pipeline will have a sequence of Pipeline components. There are two types of components - **Transformers** and **Estimators**. Transformers transforms the input Dataframe into a new DataFrame using the method `transform()`. An Estimator first fits a model to data, using the method `fit()` and then does transform. These will be more clear once you go through the below components.<br/>

#### **StringIndexer**
To build a model in Spark, the features must be of the type Double but we have a few features which are of the type String. Spark provides a Feature Transformer - StringIndexer which can be used for this transformation.
{% highlight scala %}
scala> val titleInd = new StringIndexer().setInputCol("Title").setOutputCol("TitleIndex")
titleInd: org.apache.spark.ml.feature.StringIndexer = strIdx_20dfaf280ccc
{% endhighlight %}
 Here StringIndexer is an Estimator that transforms the column Title, generates indices for the words and creates a new column named TitleIndex. Fit method of StringIndexer converts the column to StringType*(if it is not of StringType)* and then counts the occurrence of each word. It then sorts these words in descending order of their frequency and assigns an index to each word. StringIndexer.fit() method returns a StringIndexerModel which is a Transformer.
{% highlight scala %}
//execution of fit() and transform() will be done by the pipeline, this is shown to explain how fit and transform works
var strIndModel = titleInd.fit(train_data)
strIndModel: org.apache.spark.ml.feature.StringIndexerModel = strIdx_a3feab934783
{% endhighlight %}
StringIndexerModel.transform() assigns the generated index to each value of the column in the given DataFrame.
{% highlight scala %}
strIndModel.transform(train_data).select("Title","TitleIndex").show(5)
+-----+----------+
|Title|TitleIndex|
+-----+----------+
|  Mr.|       0.0|
| Mrs.|       2.0|
|Miss.|       1.0|
| Mrs.|       2.0|
|  Mr.|       0.0|
+-----+----------+
only showing top 5 rows
{% endhighlight %}
*Mr.* is the most frequent word in this data, so it is given index 0. Similarly, we will also create an indexer for the feature - *Sex*
{% highlight scala %}
val sexInd = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex")
{% endhighlight %}
<blockquote>Note that we did not call methods fit() or transform() here, that will be taken care by the Pipeline. Pipeline will execute each stage and pass the result of current stage to the next. If a stage is a Transformer, Pipeline will call transform() on it, or if it is an Estimator, pipeline will first call fit() and then transform(). But if the Estimator is the last stage in a pipeline, then the transform() won't be called.
</blockquote>

#### **Binning / Bucketing**

During Binning/Bukceting, a column with continuous values is converted into buckets. We define the start and end value of each bucket while creating the Bucketizer - *which is a Transformer*. We are going to bucketize the column 'Fare'.

{% highlight scala %}
//define the buckets/splits
val fareSplits = Array(0.0,10.0,20.0,30.0,40.0,Double.PositiveInfinity)
val fareBucketize = new Bucketizer().setInputCol("Fare").setOutputCol("FareBucketed").setSplits(fareSplits)
fareBucketize.transform(train_data).select("Fare","FareBucketed").show(10)
+-------+------------+
|   Fare|FareBucketed|
+-------+------------+
|   7.25|         0.0|
|71.2833|         4.0|
|  7.925|         0.0|
|   53.1|         4.0|
|   8.05|         0.0|
| 8.4583|         0.0|
|51.8625|         4.0|
| 21.075|         2.0|
|11.1333|         1.0|
|30.0708|         3.0|
+-------+------------+
only showing top 10 rows
{% endhighlight %}

#### **Vector Assembler**
VectorAssembler is used for assembling features into a vector. We will pass all the columns that we are going to use for the prediction to the VectorAssembler and it will create a new vector column.
{% highlight scala %}
val assembler = new VectorAssembler().setInputCols(Array("SexIndex", "Age", "TitleIndex", "Pclass", "Family","FareBucketed")).setOutputCol("features_temp")
{% endhighlight %}

#### **Normalizer**

Next we will normalize or standardize the data using the transformer - `Normalizer`. The normalizer will take the column created by the VectorAssembler, normalize it and produce a new column.
{% highlight scala %}
val normalizer = new Normalizer().setInputCol("features_temp").setOutputCol("features")
{% endhighlight %}

### **Building and Evaluating Model**
<br/>
We will be building our model using **LogisticRegression** algorithm which is used for classification. The variable that is being classified is called the dependent variable and other variables which decides the value of dependent variable are called independent variables.

In Logistic regression, based on the values of the independent variables, it predicts the probability that the dependent variable takes one of it's categorical value(classes). In our example there are two possible classes 0 or 1. To create a LogitsticRegression component,

{% highlight scala %}
val lr = new LogisticRegression().setMaxIter(10)
lr.setLabelCol("Survived")
{% endhighlight %}

#### **Create Pipeline**
Using all the components we defined till now, create a pipeline object. As already mentioned, a pipeline has set of stages and each component we add is a stage in the pipeline. Pipeline will execute each stage one after another, first executing the **fit**(if Evaluator) and then passing the result of **transform** on to the next stage.
{% highlight scala %}
val pipeline = new Pipeline().setStages(Array(sexInd, titleInd, fareBucketize, assembler, normalizer,lr))
{% endhighlight %}

#### **Training set & Test set**
To evaluate the model, we will split our data into two - training set(80%) and test set(20%). We will build our model using the training set and evaluate it using test set. We will use area under ROC curve to determine how good the model is. To split input data,
{% highlight scala %}
val splits = train_data.randomSplit(Array(0.8, 0.2), seed = 11L)
val train = splits(0).cache()
val test = splits(1).cache()
{% endhighlight %}
We will now use the pipeline to fit our training data. The result of fitting pipeline on our training data is a PipelineModel object which can be used to do prediction on test data.
{% highlight scala %}
var model = pipeline.fit(train)
model: org.apache.spark.ml.PipelineModel = pipeline_8a2ae1c4a077
var result = model.transform(test)
{% endhighlight %}
<blockquote>
  Note that the model object here is instance of PipelineModel not LogisticRegression. This is because LogisticRegression is only a component in our PipelineModel. Whenever a prediction is done for a data set, the data set has to go through all the transformations done by other components in the Pipeline before it can be used by the LogisticRegression component for prediction.
</blockquote>
To evaluate how well the model did, select the columns 'prediction' and 'Survived' from `result`, create an RDD of [(Double, Double)] and pass it on to BinaryClassificationMetrics.
{% highlight scala %}
result = result.select("prediction","Survived")
val predictionAndLabels = result.map { row =>
      (row.get(0).asInstanceOf[Double],row.get(1).asInstanceOf[Double])
    } 
val metrics = new BinaryClassificationMetrics(predictionAndLabels)
println("Area under ROC = " + metrics.areaUnderROC())
Area under ROC = 0.7757266300078556
{% endhighlight %}

Which is not bad, check this [link](http://gim.unmc.edu/dxtests/roc3.htm) to read more about how to evaluate the model based on the value of area under ROC curve.

The prediction that we did now, was on our input data where we knew the actual classification. The reason why split the data into train and test set is because we needed to compare actual result with predicted result for evaluating the model. Now will use the entire input data to train the model again.
{% highlight scala %}
model = pipeline.fit(train_data)
{% endhighlight %}

### **Doing the Prediction**
<br/>
 Download [test.csv](https://www.kaggle.com/c/titanic/download/test.csv) from Kaggle and put it in your HDFS. The test data(submission data) has to go through all loading and pre-process steps done on the training data with an additional requirement of adding the column 'Survived', because test.csv does not contain the column 'Survived'. Loading and pre-processing of test data is done using the below code:
{% highlight scala %}
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
{% endhighlight %}
Use the PipelineModel object created during model building to do the prediction.
{% highlight scala %}
result = model.transform(submission_data)
{% endhighlight %}

Let us now take a look at what our model predicted for first three passengers in test data
{% highlight scala %}
result.select("PassengerId","prediction").show(3)
+-----------+----------+
|PassengerId|prediction|
+-----------+----------+
|        892|       0.0|
|        893|       1.0|
|        894|       0.0|
+-----------+----------+
only showing top 3 rows

{% endhighlight %}
The model predicted that passengers with ID 892 and 894 will not survive and Passenger 893 will survive. 
<blockquote>Note : Received a score of <b>0.77512</b> on submitting this to Kaggle.</blockquote>

This concludes the post and I hope it was helpful. Thanks for reading.
<br/><a href="search.html?query=spark">Continue reading</a>