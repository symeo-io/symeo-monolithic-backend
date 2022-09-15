package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.infrastructure.postgres.PostgresJobAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper.entityToDomain;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresJobAdapterTestIT {

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
                return
                        generateTasksStubForRepositoriesCollection()
                        ;
            }

            @Override
            public void run() throws SymeoException {

            }

            @Override
            public void initializeTasks() throws SymeoException {

            }
        };
    }

    private List<Task> generateTasksStubForRepositoriesCollection() {
        return List.of(Task.builder()
                .input(
                        Organization.builder()
                                .name(faker.name().firstName())
                                .timeZone(TimeZone.getDefault())
                                .id(UUID.randomUUID())
                                .vcsOrganization(VcsOrganization.builder().vcsId(faker.rickAndMorty().character()).build())
                                .build()
                )
                .build());
    }

    @Test
    void should_find_all_jobs_order_by_update_date_given_a_code_and_an_organization_id() throws SymeoException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        jobRepository.save(JobEntity.builder().status(Job.FAILED).code(jobCode).organizationId(organization.getId()).tasks("[]").build());
        jobRepository.save(JobEntity.builder().status(Job.FINISHED).endDate(ZonedDateTime.now()).code(jobCode).organizationId(organization.getId()).tasks("[]").build());
        jobRepository.save(JobEntity.builder().status(Job.STARTED).code(jobCode).organizationId(organization.getId()).tasks("[]").build());

        // When
        final List<Job> jobs =
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
    void should_find_last_jobs_for_code_and_organization_and_limit() throws SymeoException, IOException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final JobEntity jobEntity1 =
                jobRepository.save(
                        JobMapper.domainToEntity(Job.builder()
                                .status(Job.FAILED).code(jobCode).organizationId(organization.getId())
                                .tasks(List.of())
                                .build())
                );

        final JobEntity jobEntity2 = jobRepository.save(
                JobMapper.domainToEntity(Job.builder()
                        .status(Job.FINISHED).endDate(new Date()).code(jobCode)
                        .organizationId(organization.getId()).tasks(List.of())
                        .tasks(List.of())
                        .build())
        );
        final JobEntity jobEntity3 = jobRepository.save(
                JobMapper.domainToEntity(Job.builder().status(Job.STARTED)
                        .code(jobCode)
                        .organizationId(organization.getId())
                        .tasks(List.of()).build()));


        // When
        final List<Job> lastJobsForCodeAndOrganizationAndLimit1 =
                postgresJobAdapter.findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCode,
                        organization, 1);
        final List<Job> lastJobsForCodeAndOrganizationAndLimit2 =
                postgresJobAdapter.findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCode,
                        organization, 2);
        final List<Job> lastJobsForCodeAndOrganizationAndLimit3 =
                postgresJobAdapter.findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCode,
                        organization, 3);

        // Then
        assertThat(lastJobsForCodeAndOrganizationAndLimit1).hasSize(1);
        assertThat(lastJobsForCodeAndOrganizationAndLimit1.get(0)).isEqualTo(entityToDomain(jobEntity3));
        assertThat(lastJobsForCodeAndOrganizationAndLimit2).hasSize(2);
        assertThat(lastJobsForCodeAndOrganizationAndLimit2.get(0)).isEqualTo(entityToDomain(jobEntity3));
        assertThat(lastJobsForCodeAndOrganizationAndLimit2.get(1)).isEqualTo(entityToDomain(jobEntity2));
        assertThat(lastJobsForCodeAndOrganizationAndLimit3).hasSize(3);
        assertThat(lastJobsForCodeAndOrganizationAndLimit3.get(0)).isEqualTo(entityToDomain(jobEntity3));
        assertThat(lastJobsForCodeAndOrganizationAndLimit3.get(1)).isEqualTo(entityToDomain(jobEntity2));
        assertThat(lastJobsForCodeAndOrganizationAndLimit3.get(2)).isEqualTo(entityToDomain(jobEntity1));
    }
}
