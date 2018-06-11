FROM flink

MAINTAINER Vishnu Viswanath <vishnu.viswanath25@gmail.com>

ADD submit.sh /

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/flink-job.jar

CMD ["/bin/bash", "/submit.sh"]