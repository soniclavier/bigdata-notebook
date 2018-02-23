package com.vishnuviswanath.spark.util

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.annotation.tailrec
import scala.util.{Random ⇒ r}
/**
  * Created by vviswanath on 1/15/18.
  */
object RandomCarsKafkaProducer {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val interval = 1000
    val topic = "cars"
    val numRecsToProduce: Option[Int] = None //None = infinite


    @tailrec
    def produceRecord(numRecToProduce: Option[Int]): Unit = {
      def generateCarRecord(topic: String): ProducerRecord[String, String] = {
        val carName = s"car${r.nextInt(10)}"
        val speed = r.nextInt(150)
        val acc = r.nextFloat * 100

        val value = s"$carName,$speed,$acc,${System.currentTimeMillis()}"
        print(s"Writing $value\n")
        val d = r.nextFloat() * 100
        if (d < 2) {
          //induce random delay
          println("Argh! some network dealy")
          Thread.sleep((d*100).toLong)
        }
        new ProducerRecord[String, String](topic,"key", value)
      }

      numRecToProduce match {
        case Some(x) if x > 0 ⇒
          producer.send(generateCarRecord(topic))
          Thread.sleep(interval)
          produceRecord(Some(x - 1))

        case None ⇒
          producer.send(generateCarRecord(topic))
          Thread.sleep(interval)
          produceRecord(None)

        case _ ⇒
      }
    }

    produceRecord(numRecsToProduce)


  }

}
