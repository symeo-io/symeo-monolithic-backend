FROM openjdk:17-alpine

WORKDIR webapp/
ADD bootstrap/target/catlean-monolithic-backend.jar webapp/catlean-monolithic-backend.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml

ENV JAVA_OPTS="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -Djava.security.egd=file:/dev/urandom"

CMD java \
    $JAVA_OPTS \
    -jar webapp/catlean-monolithic-backend.jar \
    --spring.config.additional-location=webapp/application.yaml
