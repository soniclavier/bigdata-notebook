## Flink Streaming

<blockquote>Note : print() functions used on DataStream objects will not print in the console, this will be printed in the .out file of log.
This is defined in the log4j.properties in the conf folder. Change the logger type to ConsoleAppender to print log to console.</blockquote>

All the socket based streaming jobs in the examples listen to port 4444. To simulate messages coming through this port,
run `nc -lk 4444` and send sample messages

#### SocketStreming
```
flink run target/scala-2.10/flink-vishnu_2.10-1.0.jar -c com.vishnu.flink.streaming.FlinkStreamingWordCount
```
#### Tumbling window streaming (similar to batch)
```
flink run target/scala-2.10/flink-vishnu_2.10-1.0.jar -c com.vishnu.flink.streaming.TumblingWindowStreamiming
```
