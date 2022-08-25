FROM openjdk:17-alpine

ARG dd_service
ARG dd_env
ARG spring_profiles_active

WORKDIR webapp/
ADD bootstrap/target/symeo-monolithic-backend.jar webapp/symeo-monolithic-backend.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml
ADD symeo-io.private-key.der symeo-io.private-key.der
ENV GITHUB_PRIVATE_KEY_PATH=/webapp/symeo-io.private-key.der
ADD docker-start-symeo-monolithic-backend.sh webapp/docker-start-symeo-monolithic-backend.sh
RUN chmod +x webapp/docker-start-symeo-monolithic-backend.sh

# Datadog
ENV DD_SERVICE=$dd_service
ENV DD_ENV=$dd_env
ENV SPRING_PROFILES_ACTIVE=$spring_profiles_active
RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
RUN export DD_AGENT_HOST=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)

CMD webapp/docker-start-symeo-monolithic-backend.sh
