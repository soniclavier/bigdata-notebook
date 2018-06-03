package com.vishnu.flink.util

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.annotation.tailrec
import scala.util.Random

/**
  * Created by vviswanath on 1/15/18.
  */
object RandomServerEventsKafkaProducer {

  def eventType: String = {
    Random.nextInt(3) match {
      case 0 ⇒ "cpu-usage"
      case 1 ⇒ "mem-usage"
      case 2 ⇒ "disk-usage"
    }
  }

  def serverIp: String = {
    s"192.168.23.${Random.nextInt(10)}"
  }

  def value: Double = {
    Random.nextDouble * 100
  }

  def now(possibleDelay: Boolean, maxDelay: Long): Long = {
    val now = System.currentTimeMillis()
    if (possibleDelay && Random.nextBoolean()) now - Random.nextLong % maxDelay
    else now
  }

  //returns a key,value
  def nextServerEvent: (String, String) = {

    val event = (serverIp, s"${now(possibleDelay = true, 10000)},$eventType,$serverIp,$value")
    print(s"Produced event $event\n")
    event
  }

  def main(args: Array[String]): Unit = {

    val parameters = ParameterParser.parse(args)

    val props = new Properties()
    props.put("bootstrap.servers", parameters.getOrElse("kafka-bootstrap-server", "localhost:9092"))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val interval = 10
    val topic = parameters("topic")
    val numRecsToProduce: Option[Int] = None //None = infinite


    @tailrec
    def produceRecord(numRecToProduce: Option[Int]): Unit = {
      def generateRecord(topic: String, f: ⇒ (String, String)): ProducerRecord[String, String] = {
        val event = f
        new ProducerRecord[String, String](topic, event._1, event._2)
      }

      numRecToProduce match {
        case Some(x) if x > 0 ⇒
          producer.send(generateRecord(topic, nextServerEvent))
          Thread.sleep(interval)
          produceRecord(Some(x - 1))

        case None ⇒
          producer.send(generateRecord(topic, nextServerEvent))
          Thread.sleep(interval)
          produceRecord(None)

        case _ ⇒
      }
    }

    produceRecord(numRecsToProduce)


  }

}
