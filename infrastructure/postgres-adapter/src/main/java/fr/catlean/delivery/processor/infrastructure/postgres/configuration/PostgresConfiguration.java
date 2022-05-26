package fr.catlean.delivery.processor.bootstrap.configuration;

import fr.catlean.delivery.processor.infrastructure.postgres.PostgresAdapter;
import fr.catlean.delivery.processor.infrastructure.postgres.repository.PullRequestRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {
        "fr.catlean.delivery.processor.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
        "fr.catlean.delivery.processor.infrastructure.postgres.repository"
})
@EnableTransactionManagement
@EnableJpaAuditing
public class PostgresConfiguration {

    @Bean
    public PostgresAdapter postgresAdapter(PullRequestRepository pullRequestRepository) {
        return new PostgresAdapter(pullRequestRepository);
    }

}
