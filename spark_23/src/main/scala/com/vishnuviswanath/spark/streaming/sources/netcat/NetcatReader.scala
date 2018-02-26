package com.vishnuviswanath.spark.streaming.sources.netcat

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import org.apache.spark.sql.Row
import org.apache.spark.sql.sources.v2.reader.streaming.{ContinuousDataReader, PartitionOffset}

/**
  * Created by vviswanath on 2/21/18.
  */
class NetcatReader(port: Int, host: String, buffSize: Int) extends ContinuousDataReader[Row] {

  val conn = new Socket(host, port)
  val inReader = new BufferedReader(new InputStreamReader(conn.getInputStream))

  var line: String = _


  override def next(): Boolean = {
    line = inReader.readLine()
    line != null
  }

  override def get(): Row = {
    //print(s"read value $line")
    Row(line)
  }

  override def close(): Unit = {
    conn.close()
  }

  override def getOffset: PartitionOffset = NetcatPartitionOffset(0)
}

case class NetcatPartitionOffset(offset: Long) extends PartitionOffset
