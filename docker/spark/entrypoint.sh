#!/bin/sh

case $1 in
    master) exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.master.Master
            ;;

    worker) exec $SPARK_HOME/bin/spark-class org.apache.spark.deploy.worker.Worker $2
            ;;
esac
