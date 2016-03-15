package com.vishnu.flink.dataset

import java.lang.Iterable

import org.apache.flink.api.common.functions.{GroupReduceFunction, FlatMapFunction}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.flink.api.scala._

/**
  * Created by vishnu on 3/12/16.
  * Scala equivalent for WordCount program in http://dataartisans.github.io/flink-training/dataSetBasics/slides.html
  * Reads from hdfs file, mapper emits 1 for each word and Reducer aggregates
  *
  */
object WordCount {

  def main(args:Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val data = env.readTextFile("hdfs://localhost:9000/states")

    val counts = data.flatMap(new Tokenizer())
      .groupBy(0)
      .reduceGroup(new SumWords())


    counts.print()
  }
  

}


class Tokenizer extends FlatMapFunction[String,(String,Int)] {
  override def flatMap(value: String, out: Collector[(String,Int)]): Unit = {
    val tokens = value.split("\\W+")
    for (token <- tokens if token.length>0) out.collect(token,1)
  }
}

class SumWords extends GroupReduceFunction[(String,Int),(String,Int)] {
  override def reduce(words: Iterable[(String,Int)], out: Collector[(String,Int)]): Unit = {
    var count = 0
    var prev: (String, Int) = null
    val it = words.iterator()
    while(it.hasNext) {
      prev = it.next()
      count = 1 + prev._2
    }
    out.collect(prev._1,count)
  }
}
