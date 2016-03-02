**SocketStreaming**

SocketStreaming listens to a tcp port and run word count on the data received from the stream.

To test the program,

1. run command ` nc -lk 9999`
2.  start streaming application
```
spark-submit   --class "com.vishnu.spark.streaming.SocketStreaming"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark_examples-assembly-0.1.0.jar
```
3.send some message from console at (1)

**FlumeStreaming**

1. Configure your flume-conf.properties to have sink of the type avro.
  In this example, I have used netcat as the source. see [flume-conf.properties](/Flume/src/com/vishnu/flume/config/flume-conf_spark.properties)

  Flume agent listens to port 6666 and sends the data to avro sink at port 4444
  
  Spark Streaming listens to the port 4444 and computes the word count of the incoming data
  
2. Create a fat/uber jar of the spark streaming application (needed because flume streaming jar is not present in the spark lib by default)
   - you can use assembly plugin (check [assembly.sbt](/spark/project/assembly.sbt) and [build.sbt](/spark/build.sbt))
   - run command `assembly` from sbt console

3. submit spark job using command
  ```
  spark-submit   --class "com.vishnu.spark.streaming.FlumeStreaming"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark_examples-assembly-0.1.0.jar
  ```
  
4. start the flume agent by using the command
  ```
  bin/flume-ng agent -n agent -c conf -f conf/flume-conf.properties
  ```

5. connect to the port 6666 by using curl and send some sample message
  `curl telnet://localhost:6666`
