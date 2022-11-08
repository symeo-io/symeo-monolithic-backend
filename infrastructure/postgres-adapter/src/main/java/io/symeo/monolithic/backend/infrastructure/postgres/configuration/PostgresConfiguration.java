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
                                                     final CustomPullRequestWithCommitsAndCommentsRepository customPullRequestWithCommitsAndCommentsRepository,
                                                     final CommitRepository commitRepository,
                                                     final TagRepository tagRepository,
                                                     final CustomCommitRepository customCommitRepository,
                                                     final VcsOrganizationRepository vcsOrganizationRepository,
                                                     final CustomCycleTimeRepository customCycleTimeRepository) {
        return new PostgresExpositionAdapter(pullRequestRepository, repositoryRepository,
                pullRequestTimeToMergeRepository, pullRequestSizeRepository, pullRequestFullViewRepository,
                customPullRequestViewRepository, customPullRequestWithCommitsAndCommentsRepository, commitRepository,
                tagRepository, customCommitRepository, vcsOrganizationRepository, customCycleTimeRepository);
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
    public PostgresCommitTestingDataAdapter postgresCommitTestingDataAdapter(final CommitTestingDataRepository commitTestingDataRepository) {
        return new PostgresCommitTestingDataAdapter(commitTestingDataRepository);
    }

    @Bean
    public PostgresOrganizationApiKeyAdapter postgresOrganizationApiKeyAdapter(final OrganizationApiKeyRepository organizationApiKeyRepository) {
        return new PostgresOrganizationApiKeyAdapter(organizationApiKeyRepository);
    }

    @Bean
    public CustomPullRequestViewRepository customPullRequestViewRepository(final EntityManager entityManager) {
        return new CustomPullRequestViewRepository(entityManager);
    }

    @Bean
    public CustomCommitRepository customCommitRepository(final EntityManager entityManager) {
        return new CustomCommitRepository(entityManager);
    }

    @Bean
    public CustomPullRequestWithCommitsAndCommentsRepository customPullRequestWithCommitsAndCommentsRepository(final EntityManager entityManager) {
        return new CustomPullRequestWithCommitsAndCommentsRepository(entityManager);
    }
    @Bean
    public CustomCycleTimeRepository customCycleTimeRepository(final EntityManager entityManager) {
        return new CustomCycleTimeRepository(entityManager);
    }
}
