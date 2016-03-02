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

class MapReduce {
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("MapReduce").setMaster("spark://Vishnus-MacBook-Pro.local:7077")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    //map-reduce
    var lines = sc.parallelize(List("this is", "an example"))
    val lengthOfLines = lines.map(line => line.length)
    lengthOfLines.collect()
    lines.map(line => (line.length)).reduce((a, b) => a + b) //returns an int
    lines.map(line => (line.length)).reduce(_ + _)
    lines.map(_.length).reduce(_ + _)

    //another example with string
    lines = sc.parallelize(List("this is", "an example"))
    val firstWords = lines.map(line => (line.substring(0, line.indexOf(" ")))).collect()
    //firstWords: Array[String] = Array(this, an)
    //firstWords.reduce((a,b) => a +" "+b) //returns an string
    //firstWords.reduce(_ +" "+_)

    //wordcount using flatMap and reduceByKey
    lines = sc.parallelize(List("line number one", "line number two"))
    // to show difference between map and flatMap - map does not flatten the result
    var words = lines.map(line => (line.split(" "))) 
    // difference between using collect and not using collect - collect returns an array
    var wordsFlat = lines.flatMap(line => (line.split(" "))) 
    wordsFlat.map(x => (x, 1))
    var wordCount = wordsFlat.map(x => (x, 1)).reduceByKey(_ + _)
    wordCount.collect()

    //another difference between map and flatMap
    //map should always emit an output but not required for flatMap
    lines = sc.parallelize(List("this is line number one", "line number two", "line number three"))
    lines.flatMap(_.split(" ").filter(word => word.contains("this")).map(word => (word, 1)))
    lines.map(_.split(" ").filter(word => word.contains("this")).map(word => (word, 1)))

    //filter out a line 
    lines = sc.parallelize(List("line number one", "line number two", "line number three"))
    //filtering lines
    val filtered = lines.filter(!_.contains("two"))
    //filtering in map
    var filteredAgain = filtered.map(_.split(" ").filter(!_.equals("three")))
    //filtering using flatMap
    var filteredAgainFlat = lines.flatMap(_.split(" ").filter(!_.equals("three")))

  }

}