FROM alpine
MAINTAINER Vishnu Viswanath "vishnu.viswanath25@gmail.com"

#install python
RUN apk add --update python python-dev py-pip

#install bottle 
RUN pip install bottle

#add the file to docker from context
ADD bottle_example.py /

#expose the port from docker
EXPOSE 8082

#start the application
CMD python bottle_example.py
