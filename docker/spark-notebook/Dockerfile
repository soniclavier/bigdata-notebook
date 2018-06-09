FROM openjdk:8-jre
MAINTAINER Vishnu Viswanath "vishnuviswanath.com"

RUN apt-get update && apt-get install -y curl \
    procps

ADD spark-notebook-2.2.0.tar.gz /

EXPOSE 9000

WORKDIR "/spark-notebook-2.2.0"

ENTRYPOINT ["./bin/spark-notebook"]

