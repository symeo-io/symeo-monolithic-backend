package fr.catlean.monolithic.backend.infrastructure.postgres.configuration;

import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresExpositionAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresOrganizationAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresUserAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
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
    public PostgresExpositionAdapter postgresAdapter(final PullRequestRepository pullRequestRepository,
                                                     final PullRequestHistogramRepository pullRequestHistogramRepository,
                                                     final RepositoryRepository repositoryRepository) {
        return new PostgresExpositionAdapter(pullRequestRepository, pullRequestHistogramRepository,
                repositoryRepository);
    }

    @Bean
    public PostgresUserAdapter postgresAccountAdapter(final UserRepository userRepository,
                                                      final OrganizationRepository organizationRepository) {
        return new PostgresUserAdapter(userRepository, organizationRepository);
    }

    @Bean
    public PostgresOrganizationAdapter postgresOrganizationAdapter(final OrganizationRepository organizationRepository) {
        return new PostgresOrganizationAdapter(organizationRepository);
    }

}
