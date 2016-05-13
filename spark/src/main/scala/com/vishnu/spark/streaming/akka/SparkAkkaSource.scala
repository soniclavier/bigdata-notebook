package com.vishnu.spark.streaming.akka

import org.apache.spark._
import org.apache.spark.streaming._
import akka.actor.Props
import org.apache.spark.streaming.receiver.ActorHelper
import akka.actor.Actor

/**
 * Example form http://www.lightbend.com/activator/template/spark-streaming-scala-akka#code/src/main/scala/StreamingApp.scala
 */

//INCOMPLETE

class HelloSpark extends Actor with ActorHelper {

  override def preStart() = {
    println("")
    println("Starting HelloSpark Actor")
    println("")
  }

  def receive = {
    case s => store(s)
  }
}

object SparkAkkaSource {

  //fix a driver port, this is by default random (but now we need to know what driver port is)
  val driverPort = 7777
  val driverHost = "localhost"
  val actorName = "HelloSparkActor"

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf(false)
      .setMaster("local[*]")
      .setAppName("Spark Streaming from Akka")
      .set("spark.logConf", "true")
      .set("spark.driver.port", driverPort.toString)
      .set("spark.driver.host", driverHost)
      .set("spark.akka,logLifeCycleEvents", "true")

    val ssc = new StreamingContext(conf, Seconds(1))

    val actorStream = ssc.actorStream[String](Props[HelloSpark], actorName)
    actorStream.print()

    ssc.start()
    java.util.concurrent.TimeUnit.SECONDS.sleep(3)

    val actorSystem = SparkEnv.get.actorSystem

    val url = s"akka.tcp://sparkDriver@$driverHost:$driverPort/user/vishnu/$actorName"
    val helloer = actorSystem.actorSelection(url)
    helloer ! "Hello"
    helloer ! "from"
    helloer ! "Spark Streaming"
    helloer ! "with"
    helloer ! "Scala"
    helloer ! "and"
    helloer ! "Akka"

    val ln = readLine()
    ssc.stop(stopSparkContext = true, stopGracefully = true)

  }

}