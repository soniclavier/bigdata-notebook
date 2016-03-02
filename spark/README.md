Edit your .bash_profile or .profile file in OSX/Unix or Edit your Environment variables in Windows.

**OSX/Unix:**
```
vi ~/.bash_profile
export SPARK_HOME=/Users/vishnu/spark-1.6.0-bin-hadoop2.6
export PATH=$PATH/:$SPARK_HOME/sbin
export PATH=$PATH/:$SPARK_HOME/bin
```
**To submit the application:**
```
spark-submit   --class "package.name.Object"   --master spark://your_master_server:7077 target/path/to/your/jar_file.jar
```

E.g.,

For running Titanic ML example
```
spark-submit   --class "com.vishnu.spark.kaggle.titanic.TitanicWithPipeline"   --master spark://Vishnus-MacBook-Pro.local:7077 --packages com.databricks:spark-csv_2.11:1.3.0  target/scala-2.10/spark_examples_2.10-0.1.0.jar
```

For running Streaming Example
```
spark-submit   --class "com.vishnu.spark.streaming.Socket"   --master spark://Vishnus-MacBook-Pro.local:7077 target/scala-2.10/spark_examples_2.10-0.1.0.jar
```
