FROM openjdk:17-alpine


WORKDIR webapp/
ADD bootstrap/target/catlean-monolithic-backend.jar webapp/catlean-monolithic-backend.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml
ADD catlean-io.private-key.der catlean-io.private-key.der
ENV GITHUB_PRIVATE_KEY_PATH=/webapp/catlean-io.private-key.der

# Datadog
ENV DD_SERVICE="catlean-api"
ENV DD_ENV="staging"
RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
RUN export DD_AGENT_HOST=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)

ENV JAVA_OPTS="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC \
-XX:FlightRecorderOptions=stackdepth=256 -Djava.security.egd=file:/dev/urandom \
 -Dliquibase.changelogLockPollRate=1 -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.logs.injection=true"

CMD java \
    -javaagent:/webapp/dd-java-agent.jar \
    $JAVA_OPTS \
    -jar webapp/catlean-monolithic-backend.jar \
    --spring.config.additional-location=webapp/application.yaml
