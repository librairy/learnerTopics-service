FROM anapsix/alpine-java:8u162b12_server-jre
# update packages and install docker
RUN  \
  apk update && \
  apk add docker && \
  apk add openrc && \
  rc-update add docker boot
WORKDIR /librairy
RUN chmod 777 /librairy
VOLUME /tmp
VOLUME /librairy
ADD learner-topics-service.jar /app.jar
ADD bin /librairy/model
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar
