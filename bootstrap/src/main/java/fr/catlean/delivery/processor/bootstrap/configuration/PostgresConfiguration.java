package fr.catlean.delivery.processor.bootstrap.configuration;

import fr.catlean.delivery.processor.infrastructure.postgres.PostgresAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgresConfiguration {

    @Bean
    public PostgresAdapter postgresAdapter() {
        return new PostgresAdapter();
    }
}
