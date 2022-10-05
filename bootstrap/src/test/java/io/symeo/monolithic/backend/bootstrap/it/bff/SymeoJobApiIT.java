package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForOrganizationAndTeamJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForOrganizationJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SymeoJobApiIT extends AbstractSymeoBackForFrontendApiIT {


    @Autowired
    public UserRepository userRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public OrganizationSettingsRepository organizationSettingsRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public JobRepository jobRepository;
    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();

    @Order(1)
    @Test
    void should_get_last_two_vcs_data_collection_job_status() throws InterruptedException {
        // Given
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.rickAndMorty().character())
                .build();
        organizationRepository.save(organizationEntity);
        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);
        final String repositoryId = faker.harryPotter().location();
        final RepositoryEntity repositoryEntity = repositoryRepository.save(
                RepositoryEntity.builder()
                        .defaultBranch(faker.ancient().hero())
                        .name(faker.name().lastName())
                        .organizationId(organizationId)
                        .id(repositoryId)
                        .build()
        );
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(organizationId)
                        .id(teamId)
                        .name(faker.gameOfThrones().dragon())
                        .repositoryIds(List.of(repositoryEntity.getId()))
                        .build()
        );
        final String tasks = "[{\"input\": {\"id\": \"github-512630813\", \"name\": \"symeo-webapp\", " +
                "\"defaultBranch\": \"staging\", \"organizationId\": null, \"vcsOrganizationId\": " +
                "\"github-105865802\", \"vcsOrganizationName\": \"symeo-io\"}, \"status\": \"DONE\"}, {\"input\": " +
                "{\"id\": \"github-495382833\", \"name\": \"symeo-monolithic-backend\", \"defaultBranch\": " +
                "\"staging\", \"organizationId\": null, \"vcsOrganizationId\": \"github-105865802\", " +
                "\"vcsOrganizationName\": \"symeo-io\"}, \"status\": \"DONE\"}]";
        final JobEntity expectedJob1 = JobEntity
                .builder()
                .organizationId(organizationId)
                .teamId(teamId)
                .code(CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE)
                .tasks(tasks)
                .status(Job.STARTED)
                .build();
        final JobEntity expectedJob2 = JobEntity
                .builder()
                .organizationId(organizationId)
                .code(CollectVcsDataForOrganizationJobRunnable.JOB_CODE)
                .tasks(tasks)
                .status(Job.FINISHED)
                .build();
        jobRepository.saveAll(
                List.of(
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .code(CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.STARTED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .code(CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.FAILED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .code(CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.RESTARTED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForOrganizationJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.CREATED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForOrganizationJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.FAILED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForOrganizationJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.RESTARTED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForOrganizationJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.STARTED)
                                .build()
                )
        );
        Thread.sleep(100L);
        jobRepository.save(expectedJob1);
        Thread.sleep(100L);
        jobRepository.save(expectedJob2);

        // When
        client.get()
                .uri(getApiURI(JOBS_REST_API_VCS_DATA_COLLECTION_STATUS, Map.of("organization_id",
                        organizationId.toString(),
                        "team_id", teamId.toString())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.jobs.current_job.id").isEqualTo(expectedJob2.getId())
                .jsonPath("$.jobs.current_job.status").isEqualTo(expectedJob2.getStatus())
                .jsonPath("$.jobs.current_job.code").isEqualTo(expectedJob2.getCode())
                .jsonPath("$.jobs.previous_job.id").isEqualTo(expectedJob1.getId())
                .jsonPath("$.jobs.previous_job.status").isEqualTo(expectedJob1.getStatus())
                .jsonPath("$.jobs.previous_job.code").isEqualTo(expectedJob1.getCode());
    }

}