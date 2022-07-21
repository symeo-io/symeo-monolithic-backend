package fr.catlean.monolithic.backend.bootstrap.configuration;

import io.sentry.Sentry;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"sentry"})
public class SentryConfiguration {

    @Bean
    public SentryInitiator sentryInitiator(final SentryProperties sentryProperties) {
        return new SentryInitiator(sentryProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = "bootstrap.sentry")
    public SentryProperties sentryProperties() {
        return new SentryProperties();
    }

    public static class SentryInitiator {

        public SentryInitiator(final SentryProperties sentryProperties) {
            Sentry.init(sentryOptions -> {
                sentryOptions.setDsn(sentryProperties.getDsn());
                sentryOptions.setEnvironment(sentryProperties.getEnvironment());
                sentryOptions.setServerName(sentryProperties.getServerName());
            });
        }
    }


    @Data
    public static class SentryProperties {
        String dsn;
        String environment;
        String serverName;
    }

}
