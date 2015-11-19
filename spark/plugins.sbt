addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

spark-submit \
  --class "com.vishnu.spark.bClassifier" \
  --master local[4] \
  target/scala-2.10/simple-project_2.10-1.0.jar