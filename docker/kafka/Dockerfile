FROM openjdk:8-jre
MAINTAINER Vishnu Viswanath "vishnuviswanath.com"

RUN apt-get update && apt-get install -y curl \
    procps

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

EXPOSE 2181 9092

ADD entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]