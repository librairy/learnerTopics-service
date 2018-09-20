FROM librairy/modeler-topics-service:latest
ADD model /bin
ENV SWAGGER_TITLE="Cross-lingual Topic Model by WordBased"
ENV SWAGGER_DESCRIPTION="Topic Model created from a Parallel Corpus by using a WordBased parser algorithm"
ENV SWAGGER_CONTACT_NAME="Carlos Badenes-Olmedo"
ENV SWAGGER_CONTACT_EMAIL="cbadenes@fi.upm.es"
ENV SWAGGER_CONTACT_URL="http://cbadenes.github.io/"
ENV SWAGGER_LICENSE_NAME="Apache License Version 2.0"
ENV SWAGGER_LICENSE_URL="https://www.apache.org/licenses/LICENSE-2.0"
ENTRYPOINT exec java $JAVA_OPTS -server -jar /app.jar
