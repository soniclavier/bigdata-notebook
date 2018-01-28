### Spark 2.3/2.4.0-SNAPSHOT repository

- Spark Structured Streaming
  - [Socket Stream](src/main/scala/com/vishnuviswanath/spark/streaming/SocketSourceStreaming.scala)
  - [File Stream](src/main/scala/com/vishnuviswanath/spark/streaming/HelloStructredStreaming.scala#L23)
  - [Kafka Source](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L58-L64)
  - [Kafka Sink](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L96-L109)
  - [EventTime](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L79)
  - [ProcessingTime](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L80)
  - [Watermarks](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L77)
  - [Checkpointing](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L104)
  - [Sliding Window](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L78)
  - [Tumbling Window](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L79)
  - Aggregations/Operations
     - [avg](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L81)
     - [count](src/main/scala/com/vishnuviswanath/spark/streaming/SocketSourceStreaming.scala#L37)
     - [max](src/main/scala/com/vishnuviswanath/spark/streaming/StreamingAggregations.scala#L45)
     - [alias](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L81)
     - [filter](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L82)
     - [groupby](src/main/scala/com/vishnuviswanath/spark/streaming/KafkaSourceStreaming.scala#L79)
     - [where](src/main/scala/com/vishnuviswanath/spark/streaming/StreamingAggregations.scala#L56)
  - SQL
  - Output Modes
  - Multiple Stream Queries
  - Kafka Producer
  
