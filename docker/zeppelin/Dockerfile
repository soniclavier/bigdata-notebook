FROM openjdk:8-jre
MAINTAINER Vishnu Viswanath "vishnuviswanath.com"

RUN apt-get update && apt-get install -y curl \
    procps

ENV APACHE_DOWNLOAD_URL https://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=

#ZEPPELIN
ARG ZEPPELIN_VERSION=0.7.3
ENV ZEPPELIN_PACKAGE zeppelin-${ZEPPELIN_VERSION}
ENV ZEPPELIN_DOWNLOAD_URL ${APACHE_DOWNLOAD_URL}zeppelin/${ZEPPELIN_PACKAGE}/${ZEPPELIN_PACKAGE}.tgz
ENV ZEPPELIN_HOME /usr/share/${ZEPPELIN_PACKAGE}
ENV PATH $PATH:${ZEPPELIN_HOME}/bin

RUN curl -L \
	${ZEPPELIN_DOWNLOAD_URL} \
	| gunzip \
	| tar x -C /usr/share/

WORKDIR ${ZEPPELIN_HOME}

CMD ["read", "-p", "Press enter to continue"]

