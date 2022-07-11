package fr.catlean.monolithic.backend.infrastructure.postgres.configuration;

import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.PullRequestRepository;
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
        "fr.catlean.monolithic.backend.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
        "fr.catlean.monolithic.backend.infrastructure.postgres.repository"
})
@EnableTransactionManagement
@EnableJpaAuditing
public class PostgresConfiguration {

    @Bean
    public PostgresAdapter postgresAdapter(final PullRequestRepository pullRequestRepository,
                                           final PullRequestHistogramRepository pullRequestHistogramRepository) {
        return new PostgresAdapter(pullRequestRepository, pullRequestHistogramRepository);
    }

}