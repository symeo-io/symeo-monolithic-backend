package fr.catlean.monolithic.backend.infrastructure.postgres.configuration;

import fr.catlean.monolithic.backend.infrastructure.postgres.*;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
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
    public PostgresAccountUserAdapter postgresAccountAdapter(final UserRepository userRepository,
                                                             final VcsOrganizationRepository vcsOrganizationRepository) {
        return new PostgresAccountUserAdapter(userRepository, vcsOrganizationRepository);
    }

    @Bean
    public PostgresAccountOrganizationAdapter postgresOrganizationAdapter(final VcsOrganizationRepository vcsOrganizationRepository) {
        return new PostgresAccountOrganizationAdapter(vcsOrganizationRepository);
    }

    @Bean
    public PostgresAccountTeamAdapter postgresTeamAdapter(final TeamRepository teamRepository,
                                                          final UserRepository userRepository) {
        return new PostgresAccountTeamAdapter(teamRepository, userRepository);
    }

    @Bean
    public PostgresAccountOnboardingAdapter postgresAccountOnboardingAdapter(final OnboardingRepository onboardingRepository) {
        return new PostgresAccountOnboardingAdapter(onboardingRepository);
    }

}
