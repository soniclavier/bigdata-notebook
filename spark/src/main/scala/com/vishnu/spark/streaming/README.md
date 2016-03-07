# KafkaStreaming

KafkaStreaming reads data from KafkaTopics and runs word count on that.

To run the example,

1. Start zookeeper.
2. Start kafka broker.
3. Create kafka topic.<br/>

  ```
bin/kafka-topics.sh --create --topic spark_streaming --zookeeper localhost:2181 --partitions 1 --replication-factor 1
  ```
  
    If you don't have kafka or zookeeper setup, or you would like to know how to create a topic and send messages, Check my [blog post](http://vishnuviswanath.com/realtime-storm-kafka1.html) where I have explained these w.r.t to Strom streaming, but the steps are same here aswell.
 
### Direct Streaming
DirectStream approach periodically queries the kafka topic for new offset and takes in data
from previous offset to new offset as an RDD

1.add below lines in buid.sbt. check [build.sbt](/spark/build.sbt)
```
val kafka_streaming = "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.0"
```
2.Create a fat jar. here is the [link](/spark/uberjar.md) on how to create a fat jar

3.Submit job
```
spark-submit   --class "com.vishnu.spark.streaming.KafkaDirectStream"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark-vishnu-assembly-1.0.jar
```
Send sample messages through console producer and check your console of spark job.
 
### Receiver Based approach 

    
Receiver based approach makes use of KafkaConsoleConsumer

1. Submit spark job

  ```
spark-submit   --class "com.vishnu.spark.streaming.KafkaStreaming"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark-vishnu-assembly-1.0.jar
  ```

2. Start kafka console producer
```
bin/kafka-console-producer.sh --broker localhost:9092 --topic spark_streaming
```

Send sample messages through console producer and check your console of spark job.

# SocketStreaming

SocketStreaming listens to a tcp port and run word count on the data received from the stream.

To test the program,

1. run command ` nc -lk 9999`
2.  start streaming application
```
spark-submit   --class "com.vishnu.spark.streaming.SocketStreaming"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark-vishnu-assembly-1.0.jar
```
3.send some message from console at (1)

# FlumeStreaming

1. Configure your flume-conf.properties to have sink of the type avro.
  In this example, I have used netcat as the source. see [flume-conf.properties](/Flume/src/com/vishnu/flume/config/flume-conf_spark.properties)

  Flume agent listens to port 6666 and sends the data to avro sink at port 4444
  
  Spark Streaming listens to the port 4444 and computes the word count of the incoming data
  
2. Create a fat/uber jar of the spark streaming application (needed because flume streaming jar is not present in the spark lib by default)
   - you can use assembly plugin (check [assembly.sbt](/spark/project/assembly.sbt) and [build.sbt](/spark/build.sbt))
   - run command `assembly` from sbt console

3. submit spark job using command
  ```
  spark-submit   --class "com.vishnu.spark.streaming.FlumeStreaming"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark-vishnu-assembly-1.0.jar
  ```
  
4. start the flume agent by using the command
  ```
  bin/flume-ng agent -n agent -c conf -f conf/flume-conf.properties
  ```

5. connect to the port 6666 by using curl and send some sample message
  `curl telnet://localhost:6666`
