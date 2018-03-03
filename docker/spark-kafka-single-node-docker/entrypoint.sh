#!/bin/sh

export HOSTNAME=$(hostname -i)
exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.master.Master >/dev/null 2>&1 < /dev/null &
echo "starting spark master.."
exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.worker.Worker spark://${HOSTNAME}:7077 >/dev/null 2>&1 < /dev/null &
echo "starting spark worker.."
exec $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties >/dev/null 2>&1 < /dev/null &
echo "starting zookeeper.."
exec $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties >/dev/null 2>&1 < /dev/null &
echo "starting kafka broker.."

#make container wait
exec "$@";
