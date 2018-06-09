FROM openjdk:8-jre
MAINTAINER Vishnu Viswanath "vishnuviswanath.com"

RUN apt-get update && apt-get install -y curl \
    procps

# SETUP SPARK
ARG SPARK_VERSION=2.2.1
ARG HADOOP_VERSION=2.7
ENV SPARK_PACKAGE spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}
ENV SPARK_DOWNLOAD_URL https://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz
ENV SPARK_HOME /usr/share/${SPARK_PACKAGE}
ENV PATH $PATH:${SPARK_HOME}/bin:${SPARK_HOME}/sbin

RUN curl -L \
  "${SPARK_DOWNLOAD_URL}" \
  | gunzip \
  | tar x -C /usr/share/

EXPOSE 8080 8081 6066 7077 4040 7001 7002 7003 7004 7005 7006

ADD entrypoint.sh /

WORKDIR $SPARK_HOME

ENTRYPOINT ["/entrypoint.sh"]

