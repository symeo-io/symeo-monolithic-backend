package fr.catlean.monolithic.backend.bootstrap.configuration;

import io.sentry.Sentry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class SentryConfiguration {

    @Bean
    public SentryInitiator sentryInitiator() {
        return new SentryInitiator();
    }


    public static class SentryInitiator {

        public SentryInitiator() {
            Sentry.init("https://f4857496207c4352ac9ceb448406e6a8@o1317381.ingest.sentry.io/6570536");
        }
    }

}
