package com.vishnu.flink.streaming.queryablestate

import com.vishnu.flink.streaming.queryablestate.QuerybleStateStream.ClimateLog
import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.api.common.state.{ReducingState, ReducingStateDescriptor}
import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeHint, TypeInformation}
import org.apache.flink.api.common.{ExecutionConfig, JobID}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.queryablestate.client.QueryableStateClient

import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{Await, ExecutionContext, duration}
import scala.util.{Failure, Success}

/**
  * Created by vviswanath on 3/13/17.
  */
object QueryClient {

  def main(args: Array[String]) {

    val parameterTool = ParameterTool.fromArgs(args)
    val jobId = JobID.fromHexString(parameterTool.get("jobId"))
    val key = parameterTool.get("stateKey")

    val client = new QueryableStateClient("10.0.0.189", 9067)

    val reduceFunction = new ReduceFunction[ClimateLog] {
      override def reduce(c1: ClimateLog, c2: ClimateLog): ClimateLog = {
        c1.copy(
          temperature = c1.temperature + c2.temperature,
          humidity = c1.humidity + c2.humidity)
      }
    }

    val climateLogStateDesc = new ReducingStateDescriptor[ClimateLog](
      "climate-record-state",
      reduceFunction,
      TypeInformation.of(new TypeHint[ClimateLog]() {}).createSerializer(new ExecutionConfig()))

    implicit val ec = ExecutionContext.global
    val resultFuture = toScala(client.getKvState (jobId, "queryable-climatelog-stream", key, new TypeHint[String]{}.getTypeInfo, climateLogStateDesc))

    while(!resultFuture.isCompleted) {
      println("waiting...")
      Thread.sleep(1000)
    }

    resultFuture.onComplete(r ⇒ println(r.get))
    resultFuture.onFailure(PartialFunction(println))

  }
}
