**Update:** This project uses spark 1.6, for Spark 2.3 examples click [here](https://github.com/soniclavier/bigdata-notebook/tree/master/spark_23)
Edit your .bash_profile or .profile file in OSX/Unix or Edit your Environment variables in Windows.

**OSX/Unix:**
```b
vi ~/.bash_profile
export SPARK_HOME=/Users/vishnu/spark-1.6.0-bin-hadoop2.6
export PATH=$PATH/:$SPARK_HOME/sbin
export PATH=$PATH/:$SPARK_HOME/bin
```
**To submit the application:**
```scala
//start spark master
$SPARK_HOME/sbin/start-master.sh

//start worker
//Get the spark the master url from http://localhost:8080/
$SPARK_HOME/sbin/start-slaves.sh spark://Vishnus-MacBook-Pro.local:7077

spark-submit   --class "package.name.Object"   --master spark://your_master_server:7077 target/path/to/your/jar_file.jar
```

E.g.,

For running Titanic ML example
```
spark-submit   --class "com.vishnu.spark.kaggle.titanic.TitanicWithPipeline"   --master spark://Vishnus-MacBook-Pro.local:7077 --packages com.databricks:spark-csv_2.11:1.3.0  target/scala-2.10/spark-vishnu-assemlby-1.0.jar
```

For running Streaming Example
```
spark-submit   --class "com.vishnu.spark.streaming.SocketStreaming"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark-vishnu-assemlby-1.0.jar
```
