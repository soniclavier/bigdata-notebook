package com.vishnu.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import com.stratio.datasource._
import com.stratio.datasource.mongodb._
import com.stratio.datasource.mongodb.schema._
import com.stratio.datasource.mongodb.writer._
import com.stratio.datasource.mongodb.config.MongodbConfig._
import org.apache.spark.sql.SQLContext
import com.stratio.datasource.util.Config._
import com.stratio.datasource.mongodb.config.MongodbConfigBuilder

/**
 * Using https://github.com/Stratio/Spark-MongoDB
 */
object ToMongoDB {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("ToMongoDB")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val input = sqlContext.read.json("/spark_learning/testweet.json")

    input.registerTempTable("tweets")
    val targetData = sqlContext.sql("Select * from tweets")

    val targetOutputBuilder = MongodbConfigBuilder(
      Map(Host -> List("localhost:27017"),
        Database -> "test",
        Collection -> "target",
        SamplingRatio -> 1.0,
        WriteConcern -> "normal",
        SplitKey -> "_id",
        SplitSize -> 8))

    val writeConfig =  targetOutputBuilder.build()

    // Writing data into the mongoDb table
    targetData.saveToMongodb(writeConfig)
  }

}