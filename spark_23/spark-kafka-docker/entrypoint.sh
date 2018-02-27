#!/bin/sh

case $1 in
    spark-master) exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.master.Master
            ;;

    spark-worker) exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.worker.Worker $2
            ;;

    zookeeper) exec $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
            ;;

    kafka-broker)
            sed -r -i "s/(zookeeper.connect)=(.*)/\1=$2:2181/g" $KAFKA_HOME/config/server.properties
            exec $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
            ;;

    *) echo "Unknown entrypoint $1, valid entry points are [spark-master, spark-worker <master-url>, zookeeper, kafka-broker <zk-host>]"
            ;;
esac
