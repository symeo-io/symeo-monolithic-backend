FROM openjdk:17-alpine

ARG dd_service
ARG dd_env
ARG spring_profiles_active

WORKDIR webapp/
ADD bootstrap/target/symeo-monolithic-backend.jar webapp/symeo-monolithic-backend.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml
ADD symeo-io.private-key.der symeo-io.private-key.der
ENV GITHUB_PRIVATE_KEY_PATH=/webapp/symeo-io.private-key.der

# Datadog
ENV DD_SERVICE=$dd_service
ENV DD_ENV=$dd_env
ENV SPRING_PROFILES_ACTIVE=$spring_profiles_active
RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
RUN export DD_AGENT_HOST=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)

ENV JAVA_OPTS="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC \
-XX:FlightRecorderOptions=stackdepth=256 -Djava.security.egd=file:/dev/urandom \
 -Dliquibase.changelogLockPollRate=1 -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.logs.injection=true"

CMD java \
    -javaagent:/webapp/dd-java-agent.jar \
    $JAVA_OPTS \
    -jar webapp/symeo-monolithic-backend.jar \
    --spring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    --spring.config.additional-location=webapp/application.yaml
