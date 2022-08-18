package io.symeo.monolithic.backend.domain.job;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JobManagerTest {

    private final Faker faker = new Faker();

    @Test
    void should_start_job() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final JobRunnable jobRunnableMock = mock(JobRunnable.class);
        final Job job = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(jobRunnableMock)
                .build();
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        final Job jobCreated = job.toBuilder().id(faker.number().randomNumber()).build();
        final Job jobStarted = jobCreated.started();


        // When
        when(jobStorage.createJob(jobArgumentCaptor.capture())).thenReturn(jobCreated);
        when(jobStorage.updateJob(jobStarted)).thenReturn(jobStarted);
        when(jobStorage.updateJob(jobStarted.finished())).thenReturn(jobStarted.finished());
        jobManager.start(job);

        // Then
        verify(jobStorage, times(2)).updateJob(jobArgumentCaptor.capture());
        verify(jobRunnableMock, times(1)).run();
        final List<Job> captorAllValues = jobArgumentCaptor.getAllValues();
        assertThat(captorAllValues.get(0)).isEqualTo(job);
        assertThat(captorAllValues.get(1)).isEqualTo(jobStarted);
        assertThat(captorAllValues.get(2).getCode()).isEqualTo(jobStarted.finished().getCode());
        assertThat(captorAllValues.get(2).getStatus()).isEqualTo(jobStarted.finished().getStatus());
        assertThat(captorAllValues.get(2).getOrganizationId()).isEqualTo(jobStarted.finished().getOrganizationId());
        assertThat(captorAllValues.get(2).getId()).isEqualTo(jobStarted.finished().getId());
        assertThat(captorAllValues.get(2).getEndDate()).isNotNull();
    }

    @Test
    void should_update_job_with_errors() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final JobRunnable jobRunnableMock = new JobRunnable() {
            @Override
            public void run() throws SymeoException {
                throw SymeoException.getSymeoException(faker.gameOfThrones().character(),
                        faker.dragonBall().character());
            }

            @Override
            public String getCode() {
                return null;
            }
        };
        final Job job = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(jobRunnableMock)
                .build();
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        final Job jobCreated = job.toBuilder().id(faker.number().randomNumber()).build();
        final Job jobStarted = jobCreated.started();


        // When
        when(jobStorage.createJob(jobArgumentCaptor.capture())).thenReturn(jobCreated);
        when(jobStorage.updateJob(jobStarted)).thenReturn(jobStarted);
        when(jobStorage.updateJob(jobStarted.finished())).thenReturn(jobStarted.finished());
        jobManager.start(job);

        // Then
        verify(jobStorage, times(2)).updateJob(jobArgumentCaptor.capture());
        final List<Job> captorAllValues = jobArgumentCaptor.getAllValues();
        assertThat(captorAllValues.get(0)).isEqualTo(job);
        assertThat(captorAllValues.get(1)).isEqualTo(jobStarted);
        assertThat(captorAllValues.get(2).getCode()).isEqualTo(jobStarted.failed().getCode());
        assertThat(captorAllValues.get(2).getId()).isEqualTo(jobStarted.failed().getId());
        assertThat(captorAllValues.get(2).getStatus()).isEqualTo(jobStarted.failed().getStatus());
        assertThat(captorAllValues.get(2).getOrganizationId()).isEqualTo(jobStarted.failed().getOrganizationId());
        assertThat(captorAllValues.get(2).getEndDate()).isNotNull();
    }


    @Test
    void should_start_job_and_next_job_given_a_job() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final JobRunnable jobRunnableMock = mock(JobRunnable.class);
        final JobRunnable nextJobRunnableMock = mock(JobRunnable.class);
        final Job nextJob = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(nextJobRunnableMock)
                .build();
        final Job job = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(jobRunnableMock)
                .nextJob(nextJob)
                .build();

        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        final Job jobCreated = job.toBuilder().id(faker.number().randomNumber()).build();
        final Job nextJobCreated = nextJob.toBuilder().id(faker.number().randomNumber() - 1L).build();
        final Job jobStarted = jobCreated.started();
        final Job nextJobStarted = nextJobCreated.started();


        // When
        when(jobStorage.createJob(job)).thenReturn(jobCreated);
        when(jobStorage.createJob(nextJob)).thenReturn(nextJobCreated);
        when(jobStorage.updateJob(jobStarted)).thenReturn(jobStarted);
        when(jobStorage.updateJob(nextJobStarted)).thenReturn(nextJobStarted);
        when(jobStorage.updateJob(jobStarted.finished())).thenReturn(jobStarted.finished());
        when(jobStorage.updateJob(nextJobStarted.finished())).thenReturn(nextJobStarted.finished());
        jobManager.start(job);

        // Then
        verify(jobStorage, times(4)).updateJob(jobArgumentCaptor.capture());
        verify(jobRunnableMock, times(1)).run();
        final List<Job> captorAllValues = jobArgumentCaptor.getAllValues();
        assertThat(captorAllValues.get(0)).isEqualTo(jobStarted);
        assertThat(captorAllValues.get(1).getEndDate()).isNotNull();
        assertThat(captorAllValues.get(2)).isEqualTo(nextJobStarted);
        assertThat(captorAllValues.get(3).getEndDate()).isNotNull();

    }

    @Test
    void should_find_all_jobs_order_by_update_date_desc_given_a_code_and_organization() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final Organization organization = Organization.builder().build();

        // When
        final List<Job> allJobsByCodeAndOrganizationOrderByUpdateDate =
                jobManager.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(CollectRepositoriesJobRunnable.JOB_CODE, organization);

        // Then
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(jobStorage, times(1))
                .findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(stringArgumentCaptor.capture(),
                        organizationArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
    }

    @Test
    void find_last_jobs_for_code_and_organization_and_limit() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final int numberOfJobToFind = faker.number().randomDigit();
        final String jobCode = faker.ancient().god();


        // When
        jobManager.findLastJobsForCodeAndOrganizationAndLimit(jobCode, organization, numberOfJobToFind);

        // Then
        final ArgumentCaptor<String> jobCodeCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        final ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(jobStorage, times(1)).findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCodeCaptor.capture(),
                organizationArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertThat(jobCodeCaptor.getValue()).isEqualTo(jobCode);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
        assertThat(integerArgumentCaptor.getValue()).isEqualTo(numberOfJobToFind);
    }
}