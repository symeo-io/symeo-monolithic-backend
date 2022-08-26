#!/usr/bin/env sh
set -x

if [ -z "$DD_ENV" ]; then
  echo "ERROR - Missing env parameter"
  exit 1
fi
if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
  echo "ERROR - Missing spring-profiles-active parameter"
  exit 1
fi

echo "Starting Symeo monolithic backend for env ${DD_ENV} and spring-profiles-active ${SPRING_PROFILES_ACTIVE}"
java_options="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC \
               -XX:FlightRecorderOptions=stackdepth=256 -Djava.security.egd=file:/dev/urandom \
                -Dliquibase.changelogLockPollRate=1 -Duser.timezone=\"Europe/Paris\""
if [[ $DD_ENV == "prod" ]]; then
  java_options="${java_options} -Ddd.profiling.enabled=true -Ddd.logs.injection=true -Ddd.logs.injection=true"
  java -javaagent:/webapp/dd-java-agent.jar \
    $java_options \
    -jar webapp/symeo-monolithic-backend.jar \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE \
    --spring.config.additional-location=webapp/application.yaml
else
  java $java_options \
    -jar webapp/symeo-monolithic-backend.jar \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE \
    --spring.config.additional-location=webapp/application.yaml
fi
