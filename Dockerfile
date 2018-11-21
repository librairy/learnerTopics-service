FROM registry.bdlab.minetur.es/modeler-topics-service:latest
ADD model /bin
ENV SWAGGER_TITLE="my first model"
ENV SWAGGER_DESCRIPTION="sample model"
ENV SWAGGER_CONTACT_NAME="oeg-upm"
ENV SWAGGER_CONTACT_EMAIL="info@upm.es"
ENV SWAGGER_CONTACT_URL="http://librairy.linkeddata.es"
ENV SWAGGER_LICENSE_NAME="Apache License Version 2.0"
ENV SWAGGER_LICENSE_URL="https://www.apache.org/licenses/LICENSE-2.0"
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar
