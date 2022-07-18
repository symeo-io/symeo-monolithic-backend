package fr.catlean.monolithic.backend.bootstrap.configuration;

import io.sentry.Sentry;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class SentryConfiguration {

    @Bean
    public SentryInitiator sentryInitiator(final SentryProperties sentryProperties) {
        return new SentryInitiator(sentryProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = "sentry")
    public SentryProperties sentryProperties() {
        return new SentryProperties();
    }

    public static class SentryInitiator {

        private final SentryProperties sentryProperties;

        public SentryInitiator(final SentryProperties sentryProperties) {
            this.sentryProperties = sentryProperties;
            Sentry.init(sentryProperties.getDns());
        }
    }


    @Data
    public static class SentryProperties {
        String dns;
        String environment;
        String serverName;
    }

}
