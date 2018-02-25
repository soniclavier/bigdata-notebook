#!/bin/sh

case $1 in
    spark-master) exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.master.Master
            ;;

    spark-worker) exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.worker.Worker $2
            ;;

    kafka-broker)
            echo "starting zookeeper"
            setsid $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties >/dev/null 2>&1 < /dev/null &
            sleep 2
            echo "starting kafka-broker"
            exec $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
            ;;

    *) echo "Unknown entrypoint $1, valid entry points are [spark-master, spark-worker <master-url>, kafka-broker]"
            ;;
esac
