package com.vishnuviswanath.spark.streaming.sources.netcat

import java.util
import java.util.Optional

import org.apache.spark.sql.Row
import org.apache.spark.sql.sources.v2.reader.{DataReader, DataReaderFactory}
import org.apache.spark.sql.sources.v2.reader.streaming.{ContinuousReader, Offset, PartitionOffset}
import org.apache.spark.sql.types.StructType

/**
  * Created by vviswanath on 2/21/18.
  */
class NetcatContinuousReader(schema: StructType,
                             sourceOptions: Map[String, String]) extends ContinuousReader {


  val numPartitions = 1

  private var offset: Offset = _

  override def getStartOffset: Offset = offset

  override def mergeOffsets(offsets: Array[PartitionOffset]): Offset = new NetcatOffset

  override def setStartOffset(start: Optional[Offset]): Unit = {}

  override def deserializeOffset(json: String): Offset = new NetcatOffset

  override def commit(end: Offset): Unit = {}

  /**
    * Create a reader factory with just 1 reader.
    * @return
    */
  override def createDataReaderFactories(): util.List[DataReaderFactory[Row]] = {
    java.util.Arrays.asList(new DataReaderFactory[Row] {
      val port = sourceOptions.getOrElse("port", "9999").toInt
      val host = sourceOptions.getOrElse("host", "localhost")
      val buffSize = sourceOptions.getOrElse("buffSize", "100").toInt
      override def createDataReader(): DataReader[Row] = new NetcatReader(port, host, buffSize)
    })
  }

  override def readSchema(): StructType = schema

  override def stop(): Unit = {}
}
