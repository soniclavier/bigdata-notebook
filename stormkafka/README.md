This is a POC on how to build a near Realtime Processing system using **Apache Storm** and **Kafka** in **Java**.<br/>

Messages come into a Kafka topic, Storm picks up these messages using Kafka Spout and gives it to a Bolt, 
which parses and identifies the message type based on the header. 

Once the message type is identified, the content of the message is extracted and is sent to different bolts for 
persistence - SOLR bolt, MongoDB bolt or HDFS bolt.

[view the blog](http://vishnuviswanath.com/realtime-storm-kafka1.html)
