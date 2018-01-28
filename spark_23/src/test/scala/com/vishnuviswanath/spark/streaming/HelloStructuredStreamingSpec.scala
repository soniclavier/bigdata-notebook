package com.vishnuviswanath.spark.streaming

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * Created by vviswanath on 1/10/18.
  */
class HelloStructuredStreamingSpec extends FunSuite with BeforeAndAfterEach {

  var spark: SparkSession = _

  override def beforeEach(): Unit = {
    spark = SparkSession.builder()
      .appName("unitTest")
      .master("local")
      .getOrCreate()
  }

  override def afterEach(): Unit = {
    spark.stop()
  }

  test("Hello structured streaming") {

  }

}
