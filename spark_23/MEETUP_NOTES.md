# Spark Meetup Notes/Commands
`2018-03-15`

### Slide 11: Hello World - Batch application
```scala
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by vviswanath on 3/15/18.
  */
object HelloWorld {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val names: DataFrame = spark
      .read
      .text("/Users/vviswanath/spark_meetup/names")

    val helloNames = names.as[String].map(name ⇒ s"hello $name")

    helloNames.write.format("console").save()
  }
}

```

### Slide 12: Hello World - Streaming application

```scala
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by vviswanath on 3/15/18.
  */
object HelloWorld {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val names: DataFrame = spark
      .readStream
      .text("/Users/vviswanath/spark_meetup/names")

    val helloNames = names.as[String].map(name ⇒ s"hello $name")

    val query = helloNames.writeStream.format("console").start()

    query.awaitTermination()

  }
}
```
#### example with groupby
```scala
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

/**
  * Created by vviswanath on 3/15/18.
  */
object HelloWorld {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val names: DataFrame = spark
      .readStream
      .text("/Users/vviswanath/spark_meetup/names")

    val helloNames = names.as[String].groupBy("value").agg(count("value"))

    val query = helloNames.writeStream.outputMode("complete").format("console").start()

    query.awaitTermination()

  }
}
```
## Connect to Docker/ Docker commands
`unset ${!DOCKER*}` //needed only if older version of boot2docker had some conflict with newer version

check docker processes

    docker ps -a

list docker images

    docker images
 
pull docker image
  
    docker pull soniclavier/spark-kafka-singlenode
  
launch docker intance in interactive mode, and with port forwarding

    docker run -it --name spark-kafka -p 8080:8080 -p 8081:8081 -p 4040:4040 soniclavier/spark-kafka-singlenode bash
 
incase of error like `docker: Error response from daemon: Conflict. The container name "/spark-kafka" is already in use by container "d1029a7f287c190d72b63f4a4d38ab64e6771e4c083979ca8fc1323c0308f94c". You have to remove (or rename) that container to be able to reuse that name`
indicates you already have a container with same name. Solution, use another name or rm the old container

    docker rm d1029a7f287c190d72b63f4a4d38ab64e6771e4c083979ca8fc1323c0308f94c
    
incase of error `failed: port is already allocated`, stop the application (may be in IntelliJ) that is already binding to the same port      

login to a running docker

    docker exec -it spark-kafka bash
    

`stty cols 150` to set the number of columns in shell

### slide 22. Text source

```scala
case class SiteViews(page: String, country: String, pageViews: Long, uniquePageViews: Long, avgTime: String)

object SiteViews {
def apply(str: String): Option[SiteViews] = {
try {
val parts = str.split(",")
Some(SiteViews(parts(0), parts(1), parts(2).toLong, parts(3).toLong, parts(4)))
}catch {
case e: Exception =>  {
println(s"could not parse $str")
None
}
}
}
}

val siteViews = spark.readStream.format("text").load("/data/site-views/csv/")
val siteViewsDs = siteViews.flatMap(r => SiteViews(r.getString(0)))
val query = siteViewsDs.map(sv => s"The page is ${sv.page}").writeStream.format("memory").queryName("site_views_tbl").start()
```

### Slide 23 : Json source
```scala
val viewsSchema = spark.read.format("json").load("/data/site-views/json").schema
val siteViews = spark.readStream.format("json").schema(viewsSchema).load("/data/site-views/json/")

val query = siteViews.writeStream.format("memory").queryName("site_views_tbl").start()

spark.sql("select * from site_views_tbl").show()

query.isActive
spark.streams.active.map(_.stop)
```

defining schema
```scala
import org.apache.spark.sql.types._
val viewsSchema = new StructType().
    add("Country", StringType).
    add("Pageviews", LongType, false).
    add("Unique Pageviews", LongType, false).
    add("Page", StringType, false).
    add("Avg. Time on Page", StringType, true)
    
val siteViews = spark.readStream.format("json").schema(viewsSchema).load("/data/site-views/json/")
siteViews.writeStream.format("console").start()
```
dealing with timestmap
```scala
val viewsSchema = StructType(
    Array(
      StructField("Country", StringType, true),
      StructField("Pageviews", LongType, false),
      StructField("Unique Pageviews", LongType, false),
      StructField("Page", StringType, false),
      StructField("Avg. Time on Page", TimestampType, true)))

val siteViews = spark.readStream.format("json").option("timestampFormat", "H:mm:ss").schema(viewsSchema).load("/data/site-views/json/")
val query = siteViews.writeStream.format("console").start()
```

### Slide 26- CSV source
```scala
case class Device(deviceType: String, page: String, users: Int, newUsers: Int, sessions: Int)

import org.apache.spark.sql.Encoders
val deviceSchema = Encoders.product[Device].schema
val siteDevices = spark.readStream.format("csv").option("mode", "DROPMALFORMED").schema(deviceSchema).load("/data/site-device/csv")
```

dealing with compression
```
val siteDevices = spark.readStream.format("csv").option("mode", "DROPMALFORMED").option("compression", "gzip").schema(deviceSchema).load("/data/site-device/csvcompressed")
```

### Slide 27 - Parquet
val siteDevicesSchema = spark.read.format("parquet").load("/data/site-device/parquet/").schema
val siteDevices = spark.readStream.format("parquet").schema(siteDevicesSchema).load("/data/site-device/parquet/")

## Operations

```scala
val viewsSchema = spark.read.format("json").load("/data/site-views/json").schema

val siteViews = spark.read.format("json").schema(viewsSchema).load("/data/site-views/json/")


val siteViews = spark.readStream.format("json").schema(viewsSchema).load("/data/site-views/json/")
siteViews.first #only in batch API

siteViews.columns
siteViews.col("Page")


expr("Page")
res35: org.apache.spark.sql.Column = Page

siteViews.select("Pageviews / 10")
expr("Pageviews / 10")

val query = siteViews.filter(expr("Pageviews > 100")).writeStream.format("console").start()
val query = siteViews.filter(r => r.getAs[Long]("Pageviews") > 100).writeStream.format("console").start()
val query = siteViews.map{r: Row => r.getAs[Long]("Pageviews")}.writeStream.outputMode("update").format("console").start()

val query = siteViews.groupBy("country").agg("Pageviews" -> "sum", "Unique Pageviews" -> "avg").writeStream.outputMode("update").format("console").start()

val query = siteViews.groupByKey("country").agg("Pageviews" -> "sum", "Unique Pageviews" -> "avg").writeStream.outputMode("update").format("console").start()

# ###not supported###

val query = siteViews.select(sumDistinct("Pageviews")).writeStream.outputMode("update").format("console").start()
val query = siteViews.select(avg("Pageviews").alias("avg")).select(sum("avg")).writeStream.outputMode("update").format("console").start()
```

### Dataset operations
```scala
case class Device(deviceType: String, page: String, users: Long, newUsers: Long, sessions: Long)
import org.apache.spark.sql.Encoders
val devicesSchema = Encoders.product[Device].schema
val siteDevices = spark.read.format("csv").schema(devicesSchema).option("header", true).option("mode", "DROPMALFORMED").load("/data/site-device/csv").as[Device]

val siteDevices = spark.readStream.format("csv").schema(devicesSchema).option("header", true).option("mode", "DROPMALFORMED").load("/data/site-device/csv").as[Device]

val query = siteDevices.groupBy("page").sum("users").writeStream.outputMode("update").format("console").start()

val query = siteDevices.groupByKey(d => d.page).agg(sum(col("users")).as[Long]).writeStream.format("console").outputMode("update").start()
```

### Sql operations
```scala
case class Device(deviceType: String, page: String, users: Int, newUsers: Int, sessions: Int)

import org.apache.spark.sql.Encoders
val deviceSchema = Encoders.product[Device].schema
val siteDevices = spark.readStream.format("csv").option("mode", "DROPMALFORMED").schema(deviceSchema).load("/data/site-device/csv")
val query = siteDevices.writeStream.format("memory").queryName("site_devices").start()

val viewsSchema = spark.read.format("json").load("/data/site-views/json").schema
val siteViews = spark.readStream.format("json").schema(viewsSchema).load("/data/site-views/json/")

val query2 = siteViews.writeStream.format("memory").queryName("site_views").start()

spark.sql("select * from site_devices").show()
spark.sql("select page, count(users) from site_devices where users > newUsers + 10 group by page").show()
spark.sql("select deviceType, page, sum(users), avg(sessions) from site_devices where page like '%spark%' group by page, deviceType having sum(newUsers) > 10")


joins

val query = siteDevices.where("users > newUsers + 10").groupBy("page").agg("users" -> "count").writeStream.outputMode("update").format("console").start()

spark.sql("select * from site_devices join site_views on site_devices.Page = site_views.page")
```

## Sinks
```scala
case class Device(deviceType: String, page: String, users: Int, newUsers: Int, sessions: Int)
import org.apache.spark.sql.Encoders
val deviceSchema = Encoders.product[Device].schema
val siteDevices = spark.readStream.format("csv").option("mode", "DROPMALFORMED").schema(deviceSchema).load("/data/site-device/csv")

val query = siteDevices.writeStream.format("parquet").partitionBy("deviceType").option("path", "/data/site-device/partitioned-parquet").option("checkpointLocation", "/checkpoint").start()
```

### parquet
```scala
val query = siteDevices.writeStream.format("parquet").partitionBy("deviceType").option("compression", "none").option("path", "/data/site-device/partitioned-parquet").option("checkpointLocation", "/checkpoint").start()
```

### json
```
val query = siteDevices.writeStream.format("json").partitionBy("deviceType").option("compression", "none").option("path", "/data/site-device/partitioned-json").option("checkpointLocation", "/checkpoint").start()
```
### csv
```
val query = siteDevices.writeStream.format("csv").partitionBy("deviceType").option("overwrite", "true").option("compression", "none").option("path", "/data/site-device/partitioned-json").option("checkpointLocation", "/checkpoint").start()
```
