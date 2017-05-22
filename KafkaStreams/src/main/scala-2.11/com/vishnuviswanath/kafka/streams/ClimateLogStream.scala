package com.vishnuviswanath.kafka.streams

/**
  * Created by vviswanath on 4/22/17.
  *
  * A KafkaStreams example that demonstrates use of flatMapValues, branch, predicate, selectKey, through, join and to
  * Also demonstrates the use of custom Serializer using Kryo
  *
  * flatMapValues → applies a flatMap function on the values
  * branch → creates branches on the input stream based on the predicates given (applies each predicate to the element till the first sucesss and assigns it to that stream)
  * predicate → predicate used for branching
  * through → saves the stream to a kafka topic and reads back as KStream
  * join → joins two streams
  * to → saves to a kafka topic
  */
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._

import collection.JavaConverters._
import java.lang.Iterable
import java.util

import com.esotericsoftware.kryo.io.{ByteBufferInput, Input, Output}
import com.esotericsoftware.kryo.Kryo
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}

import scala.util.DynamicVariable

object ClimateLogStream {

  def main(args: Array[String]): Unit = {
    val settings = new Properties
    settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-example")
    settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    settings.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.serdeFrom(classOf[String]).getClass.getName)
    settings.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG,  classOf[ClimateLogSerDe].getName)

    val kstreamBuilder = new KStreamBuilder
    val rawStream: KStream[String, String] = kstreamBuilder.stream(Serdes.String, Serdes.String, "climate_events")

    val climateLogStream: KStream[String, ClimateLog] = rawStream.flatMapValues(new ValueMapper[String, Iterable[ClimateLog]]{
      override def apply(value: String): Iterable[ClimateLog] = ClimateLog(value).toIterable.asJava
    })

    climateLogStream.print()

    //define the predicates to split the stream into branches
    val highHumidty = new Predicate[String, ClimateLog] {
      override def test(t: String, c: ClimateLog): Boolean = c.humidity > 50
    }

    val lowTemp = new Predicate[String, ClimateLog] {
      override def test(t: String, c: ClimateLog): Boolean = c.temperature < 0
    }

    //array of streams for each predicate
    val branches = climateLogStream.branch(highHumidty, lowTemp)

    //persists the stream onto the topic and reads back using default serde.
    val highHumidityStream = branches(0).through(new Serdes.StringSerde, new ClimateLogSerDe, "high_humidity")
    val lowTempStream = branches(1).through(new Serdes.StringSerde, new ClimateLogSerDe, "low_temp")

    highHumidityStream.print()
    lowTempStream.print()


    val keyedHighHumStream: KStream[String, ClimateLog] = highHumidityStream.selectKey(new KeyValueMapper[String, ClimateLog, String] {
      override def apply(key: String, value: ClimateLog): String = value.country
    })

    val keyedLowTempStream: KStream[String, ClimateLog] = lowTempStream.selectKey(new KeyValueMapper[String, ClimateLog, String] {
      override def apply(key: String, value: ClimateLog): String = value.country
    })

    keyedHighHumStream.print()
    keyedLowTempStream.print()

    //create a join window. This window joins all the elements of the same key if the difference between their timestamps is within 60 seconds
    val joinWindow = JoinWindows.of(60 * 1000)

    //join the streams

    val warningsStream: KStream[String, String] = keyedHighHumStream.join[ClimateLog, String](
      keyedLowTempStream,
      new ValueJoiner[ClimateLog, ClimateLog, String] {
        override def apply(value1: ClimateLog, value2: ClimateLog): String = value2.copy(humidity = value1.humidity).toString
      },
      joinWindow)

    warningsStream.print()
    warningsStream.to(new Serdes.StringSerde, new Serdes.StringSerde, "warnings")

    val streams = new KafkaStreams(kstreamBuilder, settings)

    streams.start

    Thread.sleep(4000)
    println(streams.toString())

  }


  case class ClimateLog(country: String, state: String, temperature: Float, humidity: Float) {
    override def toString = {
      s"$country, $state, $temperature, $humidity"
    }
  }
  object ClimateLog {
    def apply(line: String): Option[ClimateLog] = {
      val parts = line.split(",")
      try {
        Some(ClimateLog(parts(0), parts(1), parts(2).toFloat, parts(3).toFloat))
      } catch {
        case e: Exception => None
      }
    }
  }

  class ClimateLogKryoSerDe extends com.esotericsoftware.kryo.Serializer[ClimateLog](false) with Serializable  {
    override def write(kryo: Kryo, output: Output, `object`: ClimateLog): Unit = {
      output.writeString(`object`.country)
      output.writeString(`object`.state)
      output.writeFloat(`object`.humidity)
      output.writeFloat(`object`.temperature)
    }

    override def read(kryo: Kryo, input: Input, `type`: Class[ClimateLog]): ClimateLog = {
      val country = input.readString
      val state = input.readString
      val humidity = input.readFloat
      val temp = input.readFloat
      ClimateLog(country, state, temp, humidity)
    }
  }

  class ClimateLogSerializer extends Serializer[ClimateLog]{

    val kryos = new DynamicVariable[Kryo]({
      val kryo = new Kryo()
      kryo.addDefaultSerializer(classOf[ClimateLog], new ClimateLogKryoSerDe)
      kryo
    })

    override def serialize(topic: String, data: ClimateLog): Array[Byte] = {
      val baos = new ByteArrayOutputStream
      val output = new Output(baos)
      kryos.value.writeObject(output, data)
      output.flush
      baos.toByteArray
    }

    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {/*nothing to be done here*/}
    override def close(): Unit = {/*nothing to be done here*/}
  }

  class ClimateLogDeserializer extends Deserializer[ClimateLog] {

    val kryos = new DynamicVariable[Kryo]({
      val kryo = new Kryo()
      kryo.addDefaultSerializer(classOf[ClimateLog], new ClimateLogKryoSerDe)
      kryo
    })

    override def deserialize(topic: String, data: Array[Byte]): ClimateLog = {
      val input = new Input(new ByteArrayInputStream(data))
      kryos.value.readObject(new ByteBufferInput(data), classOf[ClimateLog])
    }

    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {/*nothing to be done here*/}
    override def close(): Unit = {/*nothing to be done here*/}
  }

  class ClimateLogSerDe extends ClimatelogWrappedSerde(new ClimateLogSerializer, new ClimateLogDeserializer)

  class ClimatelogWrappedSerde(ser: Serializer[ClimateLog], desr: Deserializer[ClimateLog]) extends Serde[ClimateLog] {

    override def deserializer(): Deserializer[ClimateLog] = desr
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
      ser.configure(configs, isKey)
      desr.configure(configs, isKey)
    }

    override def close(): Unit = {
      ser.close()
      desr.close()
    }

    override def serializer(): Serializer[ClimateLog] = ser
  }

}
