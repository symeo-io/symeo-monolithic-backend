package io.symeo.monolithic.backend.infrastructure.postgres.configuration;

import io.symeo.monolithic.backend.infrastructure.postgres.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
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
        "io.symeo.monolithic.backend.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
        "io.symeo.monolithic.backend.infrastructure.postgres.repository"
})
@EnableTransactionManagement
@EnableJpaAuditing
public class PostgresConfiguration {

    @Bean
    public PostgresExpositionAdapter postgresAdapter(final PullRequestRepository pullRequestRepository,
                                                     final RepositoryRepository repositoryRepository,
                                                     final PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository,
                                                     final PullRequestSizeRepository pullRequestSizeRepository) {
        return new PostgresExpositionAdapter(pullRequestRepository, repositoryRepository,
                pullRequestTimeToMergeRepository, pullRequestSizeRepository);
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
                                                          final UserRepository userRepository,
                                                          final TeamGoalRepository teamGoalRepository) {
        return new PostgresAccountTeamAdapter(teamRepository, userRepository, teamGoalRepository);
    }

    @Bean
    public PostgresAccountOnboardingAdapter postgresAccountOnboardingAdapter(final OnboardingRepository onboardingRepository) {
        return new PostgresAccountOnboardingAdapter(onboardingRepository);
    }

    @Bean
    public PostgresJobAdapter postgresJobAdapter(final JobRepository jobRepository) {
        return new PostgresJobAdapter(jobRepository);
    }

    @Bean
    public PostgresTeamGoalAdapter postgresTeamGoalAdapter(final TeamRepository teamRepository,
                                                           final TeamGoalRepository teamGoalRepository) {
        return new PostgresTeamGoalAdapter(teamRepository, teamGoalRepository);
    }
}