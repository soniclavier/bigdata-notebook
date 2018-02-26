package com.vishnuviswanath.spark.streaming.sources.netcat

import org.apache.spark.sql.sources.v2
import org.json4s.jackson.Serialization

/**
  * Created by vviswanath on 2/21/18.
  */
class NetcatOffset extends v2.reader.streaming.Offset {

  override def json(): String = "{}"
}
