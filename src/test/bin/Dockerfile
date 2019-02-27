FROM librairy/modeler-topics-service:latest
ADD model/model* /bin/
ENV SWAGGER_TITLE="jrc-acquis-EN-model"
ENV SWAGGER_DESCRIPTION="Collection of legislative texts (EN) from the European Union generated between years 1958 and 2006"
ENV SWAGGER_CONTACT_NAME="librAIry"
ENV SWAGGER_CONTACT_EMAIL="cbadenes@gmail.com"
ENV SWAGGER_CONTACT_URL="https://librairy.linkeddata.es"
ENV SWAGGER_LICENSE_NAME="Apache License Version 2.0"
ENV SWAGGER_LICENSE_URL="https://www.apache.org/licenses/LICENSE-2.0"
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar
