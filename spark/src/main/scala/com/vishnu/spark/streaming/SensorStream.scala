
package com.vishnu.spark.streaming

import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes

object SensorStream {

  // schema for sensor data   
  case class Sensor(resid: String, date: String, time: String, hz: Double, disp: Double, flo: Double, sedPPM: Double, psi: Double, chlPPM: Double) extends Serializable

  // function to parse line of sensor data into Sensor class

  def parseSensor(str: String): Sensor = {
    val p = str.split(",")
    Sensor(p(0), p(1), p(2), p(3).toDouble, p(4).toDouble, p(5).toDouble, p(6).toDouble, p(7).toDouble, p(8).toDouble)
  }

  val timeout = 10 // Terminate after N seconds
  val batchSeconds = 2 // Size of batch intervals
  final val tableName = "sensor"
  final val cfDataBytes = Bytes.toBytes("data")
  final val cfAlertBytes = Bytes.toBytes("alert")
  final val colHzBytes = Bytes.toBytes("hz")
  final val colDispBytes = Bytes.toBytes("disp")
  final val colFloBytes = Bytes.toBytes("flo")
  final val colSedBytes = Bytes.toBytes("sedPPM")
  final val colPsiBytes = Bytes.toBytes("psi")
  final val colChlBytes = Bytes.toBytes("chlPPM")

  def main(args: Array[String]): Unit = {
    // set up HBase Table configuration
    
    val conf = HBaseConfiguration.create
    val jobConf:JobConf = new JobConf(conf,this.getClass)
    jobConf.setOutputFormat(classOf[TableOutputFormat])
    jobConf.set(TableOutputFormat.OUTPUT_TABLE,tableName)
    
    
    val sparkConf = new SparkConf().setAppName("SensorStream")
     .set("spark.files.overwrite", "true")
    val sc = new SparkContext(sparkConf)

    // create a StreamingContext, the main entry point for all streaming functionality
    val ssc = new StreamingContext(sc, Seconds(batchSeconds))

    // parse the lines of data into sensor objects
    val textDStream = ssc.textFileStream("/user/vishnu/mapr/dev362");
    //textDStream.print()
    // TODO   parse the  dstream RDDs using the parseSensor function
    val sensorDStream = textDStream.map(parseSensor)
    // TODO  for each RDD 
    sensorDStream.foreachRDD{ rdd =>
      val alertRDD = rdd.filter(sensor => sensor.psi < 5.0)
      alertRDD.foreach(println)
      rdd.map(SensorStream.converToPut).saveAsHadoopDataset(jobConf)
      alertRDD.map(SensorStream.convertToPutAlert).saveAsHadoopDataset(jobConf)
    }


    // Start the computation
    println("start streaming")
    ssc.start()
    // Wait for the computation to terminate
    ssc.awaitTermination()

  }
  
  def converToPut(sensor: Sensor) : (ImmutableBytesWritable, Put) = {
    val dateTime = sensor.date+"_"+sensor.time
    val rowkey = sensor.resid + "_" + dateTime
    val put = new Put(Bytes.toBytes(rowkey))
    put.add(cfDataBytes, colHzBytes, Bytes.toBytes(sensor.hz))
    put.add(cfDataBytes, colDispBytes, Bytes.toBytes(sensor.disp))
    put.add(cfDataBytes, colFloBytes, Bytes.toBytes(sensor.flo))
    put.add(cfDataBytes, colSedBytes, Bytes.toBytes(sensor.sedPPM))
    put.add(cfDataBytes, colPsiBytes, Bytes.toBytes(sensor.psi))
    put.add(cfDataBytes, colChlBytes, Bytes.toBytes(sensor.chlPPM))
    return (new ImmutableBytesWritable(Bytes.toBytes(rowkey)), put)
  }
  
  def convertToPutAlert(sensor: Sensor): (ImmutableBytesWritable, Put) = {
      val dateTime = sensor.date + " " + sensor.time
      val key = sensor.resid + "_" + dateTime
      val p = new Put(Bytes.toBytes(key)) 
      p.add(cfAlertBytes, colPsiBytes, Bytes.toBytes(sensor.psi))
      
      return (new ImmutableBytesWritable(Bytes.toBytes(key)), p)
    }

}