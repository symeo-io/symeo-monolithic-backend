package fr.catlean.delivery.processor.bootstrap.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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
            "execution(* fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient" +
                    ".getRepositoriesForOrganisationName(..)) || execution(* fr.catlean.delivery.processor" +
                    ".infrastructure.github.adapter.client.GithubHttpClient" +
                    ".getPullRequestsForRepositoryAndOrganisation(..)) || execution(* fr.catlean.delivery.processor" +
                    ".infrastructure.github.adapter.client.GithubHttpClient.getPullRequestDetailsForPullRequestNumber" +
                    "(..))")
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
