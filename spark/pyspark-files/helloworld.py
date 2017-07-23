#refs : https://www.youtube.com/watch?v=08mrnJxcIWw
# 	    https://github.com/databricks/tensorframes
#Spark version 2.1.1
#bin/pyspark --master spark://vishnu-macbook-pro:7077 --packages databricks:tensorframes:0.2.8-s_2.11

import tensorflow as tf
import tensorframes as tfs

df = spark.createDataFrame(zip(range(0,10), range(1,11))).toDF("x","y")
df.show(10)

x = tfs.row(df, "x")
y = tfs.row(df, "y")

output = tf.add(x, y, name="out")

df2 = tfs.map_rows(output, df)

df2.show()