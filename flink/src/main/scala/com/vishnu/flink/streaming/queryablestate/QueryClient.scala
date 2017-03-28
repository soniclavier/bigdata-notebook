package com.vishnu.flink.streaming.queryablestate

import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.api.common.{ExecutionConfig, JobID}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.runtime.query.QueryableStateClient
import org.apache.flink.runtime.query.netty.UnknownKeyOrNamespace
import org.apache.flink.runtime.query.netty.message.KvStateRequestSerializer
import org.apache.flink.runtime.state.{VoidNamespace, VoidNamespaceSerializer}

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by vviswanath on 3/13/17.
  */
object QueryClient {

  def main(args: Array[String]) {

    val parameterTool = ParameterTool.fromArgs(args)
    val jobId = JobID.fromHexString(parameterTool.get("jobId"))
    val key = parameterTool.get("stateKey")

    val config = new Configuration
    config.setString(ConfigConstants.JOB_MANAGER_IPC_ADDRESS_KEY, "localhost")
    config.setString(ConfigConstants.JOB_MANAGER_IPC_PORT_KEY, "6123")

    val client = new QueryableStateClient(config)
    val execConfig = new ExecutionConfig
    val keySerializer = createTypeInformation[String].createSerializer(execConfig)

    //val valueSerializer = createTypeInformation[ClimateLog].createSerializer(execConfig)
    val valueSerializer = TypeInformation.of(new TypeHint[ClimateLog]() {}).createSerializer(execConfig)

    val serializedKey = KvStateRequestSerializer.serializeKeyAndNamespace(
      key,
      keySerializer,
      VoidNamespace.INSTANCE,
      VoidNamespaceSerializer.INSTANCE)

    while(true) {
      try {
        val serializedResult = client.getKvState(jobId, "climatelog-stream", key.hashCode(), serializedKey)
        serializedResult onSuccess {
          case result ⇒ {
            try {
              val clog: ClimateLog = KvStateRequestSerializer.deserializeValue(result, valueSerializer)
              println(s"State value: $clog")
            } catch {
              case e: Exception ⇒ println(s"Could not deserialize value ${e.printStackTrace()}")
            }
          }
        }
        serializedResult onFailure {
          case uk :UnknownKeyOrNamespace ⇒ println(s"Invalid stateKey ${uk.getMessage}, ${uk.printStackTrace()}")
          case e: Exception ⇒ println(s"Could not fetch KV state ${e.getMessage}, ${e.printStackTrace()}")
          }
      } catch {
        case e: Exception ⇒ println(s"Failed to get result ${e.getMessage}")
      }
      println("Send next query?")
      System.in.read()
    }
  }
}
