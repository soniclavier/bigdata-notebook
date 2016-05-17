package com.vishnu.spark.basics

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/**
 * Data from https://archive.ics.uci.edu/ml/machine-learning-databases/housing/housing.data
 */
object DataframeExample {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("HouseData")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    import sqlContext.implicits._

    val peopleRDD = sc.parallelize(List(("john", 40), ("tom", 25), ("adam", 29)))
    val peopleDF = peopleRDD.toDF("name", "age")

    val houseData = sc.textFile("/spark_learning/housing.data.txt").map(x => {
      val parts = x.trim.split("[ ]+")
      val crime = if (parts(0).trim.equals("")) -1 else parts(0).toDouble
      val zone = if (parts(1).trim.equals("")) -1 else parts(1).toDouble
      val rooms = if (parts(5).trim.equals("")) -1 else parts(5).toDouble
      val age = if (parts(6).trim.equals("")) -1 else parts(6).toDouble
      HouseInfo(crime, zone, rooms, age)
    })

    val houseDf = houseData.toDF

    houseDf.show

    //aggregation functions
    houseDf.agg(avg("age")).show
    /**
     * +-----------------+
     * |         avg(age)|
     * +-----------------+
     * |68.57490118577074|
     * +-----------------+
     */
    houseDf.agg(avg("age"), max("crime")).show
    houseDf.agg(Map("age" -> "avg", "crime" -> "max")).show
    /**
     * +-----------------+----------+
     * |         avg(age)|max(crime)|
     * +-----------------+----------+
     * |68.57490118577074|   88.9762|
     * +-----------------+----------+
     */

    //filter
    houseDf.filter("age > 50").show

    //randomsplit 
    val splits = houseDf.randomSplit(Array(0.5, 0.5), 0l)
    val part1 = splits(0)
    val part2 = splits(1)

    //joins
    part1.join(part2).show
    part1.join(part2, "zone")
    part1.join(part2, part1("zone") === part2("zone"))
    part1.join(part2, part1("zone") <=> part2("zone")) //safe for null values
    part1.join(part2, part1("zone") !== part2("zone"))
    //http://spark.apache.org/docs/latest/api/scala/#org.apache.spark.sql.Column for info on conditions 

    //groupBy
    houseDf.groupBy("zone").agg(avg("age")).show
    houseDf.groupBy(houseDf("zone")).agg(avg("age")).show

    //union
    part1.unionAll(part2)

    //select
    houseDf.select(houseDf("age")).show
    houseDf.select("age").show
    houseDf.selectExpr("age>60").show(5)
    /**
     *
     * +----------+
     * |(age > 60)|
     * +----------+
     * |      true|
     * |      true|
     * |      true|
     * |     false|
     * |     false|
     * +----------+
     */

    //sort,order
    houseDf.sort("age")
    houseDf.sort(desc("age"))
    houseDf.sort($"age".desc, $"rooms".asc).show

    houseDf.orderBy($"age".desc).show
    houseDf.orderBy($"age").show
    houseDf.orderBy("age")
    houseDf.orderBy(desc("age"))
    houseDf.orderBy(houseDf("age").desc).show

    houseDf.toJSON.take(1)
    //res164: Array[String] = Array({"crime":0.00632,"zone":18.0,"rooms":6.575,"age":65.2})

  }

  case class HouseInfo(crime: Double, zone: Double, rooms: Double, age: Double)
}