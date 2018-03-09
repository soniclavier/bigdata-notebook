FROM openjdk:8-jre
MAINTAINER Vishnu Viswanath "vishnuviswanath.com"

RUN apt-get update && apt-get install -y curl \
    procps \
    netcat

ENV APACHE_DOWNLOAD_URL https://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=

#KAFKA
ARG KAFKA_VERSION=1.0.0
ARG SCALA_VERSION=2.11
ENV KAFKA_PACKAGE kafka_${SCALA_VERSION}-${KAFKA_VERSION}
ENV KAFKA_DOWNLOAD_URL ${APACHE_DOWNLOAD_URL}kafka/${KAFKA_VERSION}/${KAFKA_PACKAGE}.tgz
ENV KAFKA_HOME /usr/share/${KAFKA_PACKAGE}
ENV PATH $PATH:${KAFKA_HOME}/bin

RUN curl -L \
    "${KAFKA_DOWNLOAD_URL}" \
    | gunzip \
    | tar x -C /usr/share/


#SPARK
ARG SPARK_VERSION=2.3.0
ARG HADOOP_VERSION=2.7
ENV SPARK_PACKAGE spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}
ENV SPARK_DOWNLOAD_URL ${APACHE_DOWNLOAD_URL}spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz
ENV SPARK_HOME /usr/share/${SPARK_PACKAGE}
ENV PATH $PATH:${SPARK_HOME}/bin:${SPARK_HOME}/sbin

RUN curl -L \
  "${SPARK_DOWNLOAD_URL}" \
  | gunzip \
  | tar x -C /usr/share/


EXPOSE 8080 8081 6066 7077 4040 7001 7002 7003 7004 7005 7006 2181 9092


RUN export COLUMNS=250

ADD data /data
ADD spark_23-assembly-1.0.jar /examples/
ADD entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]