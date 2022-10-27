package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.JobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectVcsDataForRepositoriesAndDatesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper.entityToBffDomain;
import static org.assertj.core.api.Assertions.assertThat;

public class PostgresJobAdapterTestIT extends AbstractPostgresIT {

    @Autowired
    public JobRepository jobRepository;
    private final Faker faker = new Faker();

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
    }

    @Test
    void should_save_and_update_job() throws SymeoException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final UUID organizationId = UUID.randomUUID();
        final Job job = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(generateJobRunnableStubForRepositoriesCollectionJob())
                .build();

        // When
        final Job jobCreated = postgresJobAdapter.createJob(job);
        final Job jobFinished = job.toBuilder().id(jobCreated.getId()).build().finished();
        final Job jobFinishedUpdated = postgresJobAdapter.updateJob(jobFinished);

        // Then
        final List<JobEntity> all = jobRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(jobCreated.getId()).isNotNull();
        assertThat(jobFinishedUpdated.getEndDate()).isNotNull();
        assertThat(jobCreated.getId()).isEqualTo(jobFinishedUpdated.getId());
    }

    private JobRunnable generateJobRunnableStubForRepositoriesCollectionJob() {
        return new JobRunnable() {

            @Override
            public String getCode() {
                return CollectRepositoriesJobRunnable.JOB_CODE;
            }

            @Override
            public List<Task> getTasks() {
                return List.of(Task.newTaskForInput(
                        VcsOrganization.builder()
                                .vcsId(faker.pokemon().location())
                                .externalId(faker.ancient().god())
                                .name(faker.name().firstName())
                                .organizationId(UUID.randomUUID())
                                .id(faker.number().randomNumber())
                                .build()
                ));
            }

            @Override
            public void run(final Long jobId) throws SymeoException {

            }

            @Override
            public void initializeTasks() throws SymeoException {

            }
        };
    }

    @Test
    void should_find_all_jobs_order_by_update_date_given_a_code_and_an_organization_id() throws SymeoException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        jobRepository.save(JobEntity.builder().status(Job.FAILED).code(jobCode).organizationId(organization.getId()
        ).tasks("[]").build());
        jobRepository.save(JobEntity.builder().status(Job.FINISHED).endDate(ZonedDateTime.now()).code(jobCode)
                .organizationId(organization.getId()).tasks("[]").build());
        jobRepository.save(JobEntity.builder().status(Job.STARTED).code(jobCode).organizationId(organization.getId
                ()).tasks("[]").build());

        // When
        final List<JobView> jobs =
                postgresJobAdapter.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(
                        jobCode, organization
                );

        // Then
        assertThat(jobs).isNotEmpty();
        assertThat(jobs).hasSize(3);
        assertThat(jobs.get(0).getStatus()).isEqualTo(Job.STARTED);
        assertThat(jobs.get(1).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(jobs.get(2).getStatus()).isEqualTo(Job.FAILED);
    }

    @Test
    void should_find_last_jobs_for_code_and_organization_and_limit() throws SymeoException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID organizationId = organization.getId();
        final UUID teamId = UUID.randomUUID();
        final JobEntity jobEntity1 =
                jobRepository.save(
                        JobMapper.domainToEntity(Job.builder()
                                .status(Job.FAILED).code(jobCode).organizationId(organizationId)
                                .tasks(List.of())
                                .build())
                );

        final JobEntity jobEntity2 = jobRepository.save(
                JobMapper.domainToEntity(Job.builder()
                        .status(Job.FINISHED).endDate(new Date()).code(jobCode)
                        .organizationId(organizationId).tasks(List.of())
                        .tasks(List.of())
                        .build())
        );
        final JobEntity jobEntity3 = jobRepository.save(
                JobMapper.domainToEntity(Job.builder().status(Job.STARTED)
                        .code(jobCode)
                        .organizationId(organizationId)
                        .tasks(List.of()).build()));


        // When
        final List<JobView> lastJobsForCodeAndOrganizationAndLimit1 =
                postgresJobAdapter.findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(jobCode,
                        organizationId, teamId, 1);
        final List<JobView> lastJobsForCodeAndOrganizationAndLimit2 =
                postgresJobAdapter.findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(jobCode,
                        organizationId, teamId, 2);
        final List<JobView> lastJobsForCodeAndOrganizationAndLimit3 =
                postgresJobAdapter.findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(jobCode,
                        organizationId, teamId, 3);

        // Then
        assertThat(lastJobsForCodeAndOrganizationAndLimit1).hasSize(1);
        assertThat(lastJobsForCodeAndOrganizationAndLimit1.get(0)).isEqualTo(entityToBffDomain(jobEntity3));
        assertThat(lastJobsForCodeAndOrganizationAndLimit2).hasSize(2);
        assertThat(lastJobsForCodeAndOrganizationAndLimit2.get(0)).isEqualTo(entityToBffDomain(jobEntity3));
        assertThat(lastJobsForCodeAndOrganizationAndLimit2.get(1)).isEqualTo(entityToBffDomain(jobEntity2));
        assertThat(lastJobsForCodeAndOrganizationAndLimit3).hasSize(3);
        assertThat(lastJobsForCodeAndOrganizationAndLimit3.get(0)).isEqualTo(entityToBffDomain(jobEntity3));
        assertThat(lastJobsForCodeAndOrganizationAndLimit3.get(1)).isEqualTo(entityToBffDomain(jobEntity2));
        assertThat(lastJobsForCodeAndOrganizationAndLimit3.get(2)).isEqualTo(entityToBffDomain(jobEntity1));
    }


    @Test
    void should_update_job_with_tasks_given_a_job_id() throws SymeoException {
        // Given
        final JobEntity jobEntity = jobRepository.save(
                JobEntity
                        .builder()
                        .organizationId(UUID.randomUUID())
                        .code(faker.dragonBall().character())
                        .tasks("{}")
                        .status(Job.CREATED)
                        .build()
        );
        final Long id = jobEntity.getId();
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final List<Task> tasks = List.of(Task.builder().input(2).build());

        // When
        postgresJobAdapter.updateJobWithTasksForJobId(id, tasks);

        // Then
        assertThat(jobRepository.findById(id).get().getTasks().replace(" ", ""))
                .isEqualTo(JobMapper.mapTasksToJsonString(tasks));
    }


    @Test
    void should_find_last_two_jobs_in_progress_or_finished_for_vcs_data_collection_job() throws SymeoException,
            InterruptedException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final UUID organizationId = UUID.randomUUID();
        final UUID teamId = UUID.randomUUID();
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
                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                .tasks(tasks)
                .status(Job.STARTED)
                .build();
        final JobEntity expectedJob2 = JobEntity
                .builder()
                .organizationId(organizationId)
                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                .tasks(tasks)
                .status(Job.FINISHED)
                .build();
        jobRepository.saveAll(
                List.of(
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.STARTED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.FAILED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.RESTARTED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.CREATED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.FAILED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                                .tasks(tasks)
                                .status(Job.RESTARTED)
                                .build(),
                        JobEntity
                                .builder()
                                .organizationId(organizationId)
                                .code(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
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
        final List<JobView> lastTwoJobsInProgressOrFinishedForVcsDataCollectionJob =
                postgresJobAdapter.findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(organizationId, teamId);

        // Then
        assertThat(lastTwoJobsInProgressOrFinishedForVcsDataCollectionJob).hasSize(2);
        assertThat(lastTwoJobsInProgressOrFinishedForVcsDataCollectionJob.get(0)).isEqualTo(entityToBffDomain
                (expectedJob2));
        assertThat(lastTwoJobsInProgressOrFinishedForVcsDataCollectionJob.get(1)).isEqualTo(entityToBffDomain
                (expectedJob1));
    }
}
