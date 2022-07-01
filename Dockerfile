FROM openjdk:17-alpine

WORKDIR webapp/
ADD bootstrap/target/catlean-monolithic-backend.jar webapp/catlean-monolithic-backend.jar
ADD docker-compose-resources/application.yaml webapp/application.yaml

CMD java \
    -jar webapp/catlean-monolithic-backend.jar \
    --spring.config.additional-location=webapp/application.yaml
