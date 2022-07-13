FROM openjdk:17-alpine

WORKDIR webapp/
ADD bootstrap/target/catlean-monolithic-backend.jar webapp/catlean-monolithic-backend.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml
ADD logging.properties webapp/logging.properties

ENV JAVA_OPTS="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -Djava.security.egd=file:/dev/urandom -Dliquibase.changelogLockPollRate=1"
ENV LOGGING_OPTS="-Djava.util.logging.config.file=/path/to/app.properties"

CMD java \
    $JAVA_OPTS \
    $LOGGING_OPTS \
    -jar webapp/catlean-monolithic-backend.jar \
    --spring.config.additional-location=webapp/application.yaml
