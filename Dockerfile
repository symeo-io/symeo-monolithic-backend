FROM openjdk:17-alpine

ENV GITHUB_PRIVATE_KEY_DER = "catlean-io.private-key.der"
WORKDIR webapp/
ADD bootstrap/target/catlean-monolithic-backend.jar webapp/catlean-monolithic-backend.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml
ADD logging.properties webapp/logging.properties
ADD $GITHUB_PRIVATE_KEY_DER webapp/$GITHUB_PRIVATE_KEY_DER
RUN export GITHUB_PRIVATE_KEY_PATH=$(echo $(pwd)/webapp/$GITHUB_PRIVATE_KEY_DER)

ENV JAVA_OPTS="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -Djava.security.egd=file:/dev/urandom -Dliquibase.changelogLockPollRate=1"

CMD java \
    $JAVA_OPTS \
    -jar webapp/catlean-monolithic-backend.jar \
    --spring.config.additional-location=webapp/application.yaml
