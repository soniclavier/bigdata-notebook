package com.vishnuviswanath.spark.streaming
import com.vishnuviswanath.spark.streaming.sources.netcat.NetcatSourceProvider
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

/**
  * Created by vviswanath on 2/20/18.
  *
  * An example that uses CustomV2 source {@link NetcatSourceProvider}
  */
object CustomV2SourceExample {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("CustomV2 source")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._
    val raw = spark
      .readStream
      .format(classOf[NetcatSourceProvider].getName)
      .option("port", 9999)
      .option("host", "localhost")
      .option("buffSize", 100)
      .load()

    val consoleQuery = raw
      .selectExpr("cast(value as STRING)")
      .writeStream
      .queryName("console-query")
      .format("console")
      .outputMode("update")
      //.outputMode("update")
      //.outputMode("complete") not supported since it requires an agg, and Continuous processing does not support aggregations.
      .trigger(Trigger.Continuous("3 second")) //how often to checkpoint
      .start()

    consoleQuery.awaitTermination()

  }
}
