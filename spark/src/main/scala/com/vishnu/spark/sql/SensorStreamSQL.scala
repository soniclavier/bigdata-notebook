package com.vishnu.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext

object SensorStreamSQL {
  
  def main(args: Array[String]): Unit = {
    
    //set up the contexts
    val conf = new SparkConf().setAppName("SensorStreamSQL").set("spark.files.overwrite", "true")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc,Seconds(2))
    val sqlContext = SQLContextSingleton.getInstance(sc)
    import sqlContext.implicits._
    
    //stream from text file
    val linesDStream = ssc.textFileStream("/user/vishnu/mapr/dev362");
    val sensorDStream = linesDStream.map(parseSensor)
    
    //pump vendor and maintenance data
    sc.textFile("/mapr_lab_data/data/sensorvendor.csv").map(parseVendor).toDF.registerTempTable("pump")
    sc.textFile("/mapr_lab_data/data/sensormaint.csv").map(parseMaintainence).toDF.registerTempTable("maint")
    
    sensorDStream.foreachRDD(rdd => {
      rdd.filter { sensor => sensor.psi < 5.0 }.toDF.registerTempTable("alert")
      val alertPumpMaint = sqlContext.sql("select a.resid,a.date,a.psi,p.pumpType,p.vendor,m.date,m.technician from alert a join pump p on a.resid = p.resid join maint m on p.resid = m.resid")
      alertPumpMaint.show()
    })
    
    println("start streaming")
    ssc.start
    ssc.awaitTermination
    
    
  }
  
  case class Sensor(resid: String, date: String, time: String, hz: Double, disp: Double, flo: Double, sedPPM: Double, psi: Double, chlPPM: Double) extends Serializable
  case class Maintainance(resid: String,date: String, technician: String, description: String)
  case class Vendor(resid: String, pumpType: String, purchaseDate: String, serviceDate: String, vendor: String, longitude: Float, lattitude: Float)
  
  def parseSensor(str: String): Sensor = {
    val p = str.split(",")
    Sensor(p(0), p(1), p(2), p(3).toDouble, p(4).toDouble, p(5).toDouble, p(6).toDouble, p(7).toDouble, p(8).toDouble)
  }
  
  def parseVendor(str: String): Vendor = {
    val p = str.split(",")
    Vendor(p(0), p(1), p(2), p(3), p(4), p(5).toFloat, p(6).toFloat)
  }
  
  def parseMaintainence(str: String): Maintainance = {
    val p = str.split(",")
    Maintainance(p(0), p(1), p(2), p(3))
  }
  
  
  object SQLContextSingleton {
    @transient private var instance: SQLContext = _

    def getInstance(sparkContext: SparkContext): SQLContext = {
      if (instance == null) {
        instance = new SQLContext(sparkContext)
      }
      instance
    }
  }
  
}