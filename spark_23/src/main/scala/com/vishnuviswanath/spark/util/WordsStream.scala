package com.vishnuviswanath.spark.util

/**
  * Created by vviswanath on 2/22/18.
  */
object WordsStream {

  val content = "Structured Streaming is a scalable and fault-tolerant stream processing engine built on the Spark SQL engine. You can express your streaming computation the same way you would express a batch computation on static data. The Spark SQL engine will take care of running it incrementally and continuously and updating the final result as streaming data continues to arrive. You can use the Dataset/DataFrame API in Scala, Java, Python or R to express streaming aggregations, event-time windows, stream-to-batch joins, etc. The computation is executed on the same optimized Spark SQL engine. Finally, the system ensures end-to-end exactly-once fault-tolerance guarantees through checkpointing and Write Ahead Logs. In short, Structured Streaming provides fast, scalable, fault-tolerant, end-to-end exactly-once stream processing without the user having to reason about streaming.".split(" ")

  def infiniteWordsStream(content: Array[String], index: Int): Stream[String] = {
    val nextIndex = if (index == content.length - 1) 0 else index + 1
    content(index) #:: infiniteWordsStream(content, nextIndex)
  }

  val stream = infiniteWordsStream(content, 0)
}
