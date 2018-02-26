package com.vishnuviswanath.spark.streaming.sources.netcat

import java.util.Optional

import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.sources.v2.{ContinuousReadSupport, DataSourceOptions}
import org.apache.spark.sql.sources.v2.reader.streaming.ContinuousReader
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import scala.collection.JavaConverters._

/**
  * Created by vviswanath on 2/21/18.
  */
class NetcatSourceProvider extends ContinuousReadSupport
  with DataSourceRegister {

  override def shortName(): String = "netcat"

  val netcatSchema: StructType = StructType(Seq(StructField("value", StringType)))

  override def createContinuousReader(schema: Optional[StructType], checkpointLocation: String, options: DataSourceOptions): ContinuousReader = {
    new NetcatContinuousReader(netcatSchema, options.asMap().asScala.toMap)
  }
}