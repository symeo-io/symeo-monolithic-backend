package io.symeo.monolithic.backend.infrastructure.postgres.configuration;

import io.symeo.monolithic.backend.infrastructure.postgres.adapter.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;

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
                                                     final PullRequestSizeRepository pullRequestSizeRepository,
                                                     final PullRequestFullViewRepository pullRequestFullViewRepository,
                                                     final CustomPullRequestViewRepository customPullRequestViewRepository,
                                                     final PullRequestWithCommitsAndCommentsRepository pullRequestWithCommitsAndCommentsRepository,
                                                     final CommitRepository commitRepository,
                                                     final TagRepository tagRepository) {
        return new PostgresExpositionAdapter(pullRequestRepository, repositoryRepository,
                pullRequestTimeToMergeRepository, pullRequestSizeRepository, pullRequestFullViewRepository,
                customPullRequestViewRepository, pullRequestWithCommitsAndCommentsRepository, commitRepository,
                tagRepository);
    }

    @Bean
    public PostgresUserAdapter postgresAccountAdapter(final UserRepository userRepository,
                                                      final VcsOrganizationRepository vcsOrganizationRepository) {
        return new PostgresUserAdapter(userRepository, vcsOrganizationRepository);
    }

    @Bean
    public PostgresOrganizationAdapter postgresOrganizationAdapter(final VcsOrganizationRepository vcsOrganizationRepository,
                                                                   final OrganizationRepository organizationRepository,
                                                                   final OrganizationSettingsRepository organizationSettingsRepository) {
        return new PostgresOrganizationAdapter(vcsOrganizationRepository, organizationRepository,
                organizationSettingsRepository);
    }

    @Bean
    public PostgresTeamAdapter postgresTeamAdapter(final TeamRepository teamRepository,
                                                   final UserRepository userRepository,
                                                   final TeamGoalRepository teamGoalRepository) {
        return new PostgresTeamAdapter(teamRepository, userRepository, teamGoalRepository);
    }

    @Bean
    public PostgresOnboardingAdapter postgresAccountOnboardingAdapter(final OnboardingRepository onboardingRepository) {
        return new PostgresOnboardingAdapter(onboardingRepository);
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

    @Bean
    public CustomPullRequestViewRepository customPullRequestViewRepository(final EntityManager entityManager) {
        return new CustomPullRequestViewRepository(entityManager);
    }
}
