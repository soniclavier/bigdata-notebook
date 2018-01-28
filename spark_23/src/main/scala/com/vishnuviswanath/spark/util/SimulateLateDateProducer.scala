package com.vishnuviswanath.spark.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Properties}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.annotation.tailrec
import scala.util.{Random => r}

/**
  * Created by vviswanath on 1/15/18.
  */
object SimulateLateDateProducer {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val topic = "cars"
    var mCount = 1

    def generateCarRecord(carName: String, speed: Int = r.nextInt(150), topic: String = topic, lateby: Long = 0): ProducerRecord[String, String] = {
      val acc = r.nextFloat * 100
      val nowTs = System.currentTimeMillis()
      val ts = nowTs - lateby
      val value = s"$carName,$speed,$acc,$ts"
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      val cal = Calendar.getInstance()
      cal.setTimeInMillis(ts)

      val now = Calendar.getInstance()
      now.setTimeInMillis(nowTs)
      print(s"[$mCount] Writing $value at ${format.format(now.getTime)} with Event time = ${format.format(cal.getTime)}\n")
      mCount += 1
      new ProducerRecord[String, String](topic,"key", value)
    }

    producer.send(generateCarRecord("car1", speed = 75))
    Thread.sleep(1000)
    producer.send(generateCarRecord("car2", speed = 20))
    Thread.sleep(1000)
    producer.send(generateCarRecord("car2", speed = 20))
    Thread.sleep(8000)
    producer.send(generateCarRecord("car2", speed = 20))  //this message has a hidden importance, it increments the event time
    Thread.sleep(3000)
    producer.send(generateCarRecord("car1", speed = 50, lateby = 12000))

     /*
     this will not throw away the state for the last message even though its past the watermark, since the eventtime never got updated in between
    producer.send(generateCarRecord("car1", speed = 75))
    Thread.sleep(1000)
    producer.send(generateCarRecord("car2", speed = 20))
    Thread.sleep(1000)
    producer.send(generateCarRecord("car2", speed = 20))
    Thread.sleep(1000)
    producer.send(generateCarRecord("car2", speed = 20))
    Thread.sleep(8000)
    producer.send(generateCarRecord("car1", speed = 50, lateby = 12000))*/
  }

}
