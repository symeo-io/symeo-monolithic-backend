package fr.catlean.monolithic.backend.bootstrap.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@EnableAspectJAutoProxy
@Aspect
public class CatleanDeliveryProcessorConfiguration {

    @Around(
            "execution(* fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient.getRepositoriesForOrganizationName(..)) || execution(* fr.catlean.monolithic.backend" +
                    ".infrastructure.github.adapter.client.GithubHttpClient" +
                    ".getPullRequestsForRepositoryAndOrganization(..)) " +
                    "|| execution(* fr.catlean.monolithic.backend.infrastructure.github.adapter.client" +
                    ".GithubHttpClient.getPullRequestDetailsForPullRequestNumber" +
                    "(..))"
            + "|| execution(* fr.catlean.monolithic.backend.infrastructure.postgres.*.*(..))"
    )
    public Object around(ProceedingJoinPoint point) throws Throwable {
        final StopWatch stopWatch = new StopWatch("aop-stopwatch");
        stopWatch.start();
        Object result = point.proceed();
        stopWatch.stop();
        LOGGER.info(
                "Method {} with arguments {} executed in {} s.",
                point.getSignature().getName(),
                Arrays.stream(point.getArgs())
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")),
                stopWatch.getTotalTimeSeconds());
        return result;
    }
}
