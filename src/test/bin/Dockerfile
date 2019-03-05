FROM librairy/modeler-topics-service:latest
ADD model/model* /bin/
ENV SWAGGER_TITLE="test-model"
ENV SWAGGER_DESCRIPTION="test topic model"
ENV SWAGGER_CONTACT_NAME="librAIry"
ENV SWAGGER_CONTACT_EMAIL="cbadenes@fi.upm.es"
ENV SWAGGER_CONTACT_URL="https://librairy.linkeddata.es"
ENV SWAGGER_LICENSE_NAME="Apache License Version 2.0"
ENV SWAGGER_LICENSE_URL="https://www.apache.org/licenses/LICENSE-2.0"
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar
