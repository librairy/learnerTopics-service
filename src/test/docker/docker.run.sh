docker stop learner-topics
docker rm learner-topics
docker pull librairy/learner-topics-service:latest
docker run -d --restart=always --name learner-topics -e REST_PATH=/learner  -e JAVA_OPTS=-Xmx53248m -e NLP_ENDPOINT=http://librairy.linkeddata.es/%% -v `pwd`/corpus:/librairy -v /var/run/docker.sock:/var/run/docker.sock -p 7799:7777 li
brairy/learner-topics-service:latest
docker logs -f learner-topics