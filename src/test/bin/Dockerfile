FROM registry.bdlab.minetur.es/modeler-topics-service:latest
ADD model/model* /bin/
ENV SWAGGER_TITLE="my title"
ENV SWAGGER_DESCRIPTION="string"
ENV SWAGGER_CONTACT_NAME="my name"
ENV SWAGGER_CONTACT_EMAIL="sample@mail.com"
ENV SWAGGER_CONTACT_URL="string"
ENV SWAGGER_LICENSE_NAME="string"
ENV SWAGGER_LICENSE_URL="string"
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar
