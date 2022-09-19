package io.symeo.monolithic.backend.domain.job;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
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
                .tasks(List.of())
                .id(2L)
                .build();
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        final Job jobCreated = job.toBuilder().id(faker.number().randomNumber()).build();
        final Job jobStarted = jobCreated.started();
        final Job jobFinished = jobStarted.finished();


        // When
        when(jobStorage.createJob(jobArgumentCaptor.capture())).thenReturn(jobCreated);
        when(jobStorage.updateJob(jobStarted)).thenReturn(jobStarted);
        when(jobStorage.updateJob(jobFinished)).thenReturn(jobFinished);
        jobManager.start(job);

        // Then
        verify(jobStorage, times(2)).updateJob(jobArgumentCaptor.capture());
        verify(jobRunnableMock, times(1)).run(jobStarted.getId());
        verify(jobRunnableMock, times(1)).initializeTasks();
        final List<Job> captorAllValues = jobArgumentCaptor.getAllValues();
        assertThat(captorAllValues.get(0)).isEqualTo(job);
        assertThat(captorAllValues.get(1)).isEqualTo(jobStarted);
        assertThat(captorAllValues.get(2).getCode()).isEqualTo(jobFinished.getCode());
        assertThat(captorAllValues.get(2).getStatus()).isEqualTo(jobFinished.getStatus());
        assertThat(captorAllValues.get(2).getOrganizationId()).isEqualTo(jobFinished.getOrganizationId());
        assertThat(captorAllValues.get(2).getId()).isEqualTo(jobFinished.getId());
        assertThat(captorAllValues.get(2).getEndDate()).isNotNull();
    }

    @Test
    void should_update_job_with_errors() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final SymeoException symeoException =
                SymeoException.getSymeoException(faker.gameOfThrones().character(),
                        faker.dragonBall().character(), new NullPointerException());
        final Task expectedTask = Task.builder().input(faker.name().firstName()).build();
        final JobRunnable jobRunnableMock = new JobRunnable() {
            @Override
            public void run(final Long jobId) throws SymeoException {
                throw symeoException;
            }

            @Override
            public void initializeTasks() throws SymeoException {

            }

            @Override
            public String getCode() {
                return null;
            }

            @Override
            public List<Task> getTasks() {
                return List.of(expectedTask);
            }

        };
        final Job job = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(jobRunnableMock)
                .tasks(List.of())
                .build();
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        final Job jobCreated = job.toBuilder().id(faker.number().randomNumber()).build();
        final Job jobStarted = jobCreated.started();
        final Job jobFinished = jobStarted.finished();


        // When
        when(jobStorage.createJob(jobArgumentCaptor.capture())).thenReturn(jobCreated);
        when(jobStorage.updateJob(jobStarted)).thenReturn(jobStarted);
        when(jobStorage.updateJob(jobFinished)).thenReturn(jobFinished);
        jobManager.start(job);

        // Then
        verify(jobStorage, times(2)).updateJob(jobArgumentCaptor.capture());
        final List<Job> captorAllValues = jobArgumentCaptor.getAllValues();
        assertThat(captorAllValues.get(0)).isEqualTo(job);
        assertThat(captorAllValues.get(1)).isEqualTo(jobStarted);
        final Job failed = jobStarted.failed(symeoException);
        assertThat(captorAllValues.get(2).getCode()).isEqualTo(failed.getCode());
        assertThat(captorAllValues.get(2).getId()).isEqualTo(failed.getId());
        assertThat(captorAllValues.get(2).getStatus()).isEqualTo(failed.getStatus());
        assertThat(captorAllValues.get(2).getOrganizationId()).isEqualTo(failed.getOrganizationId());
        assertThat(captorAllValues.get(2).getEndDate()).isNotNull();
        assertThat(captorAllValues.get(2).getError()).isEqualTo(symeoException.toString());
        assertThat(captorAllValues.get(2).getTasks()).isEqualTo(List.of(expectedTask));
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
        final List<Task> expectedTasks = List.of(
                Task.builder().input(faker.name().firstName()).build(),
                Task.builder().input(faker.name().firstName()).build()
        );
        final List<Task> nextExpectedTasks = List.of(
                Task.builder().input(faker.name().firstName()).build(),
                Task.builder().input(faker.name().firstName()).build()
        );
        final Job nextJob = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(nextJobRunnableMock)
                .tasks(nextExpectedTasks)
                .id(4L)
                .build();
        final Job job = Job.builder()
                .organizationId(organizationId)
                .jobRunnable(jobRunnableMock)
                .nextJob(nextJob)
                .tasks(expectedTasks)
                .id(2L)
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
        when(jobRunnableMock.getTasks()).thenReturn(expectedTasks);
        when(nextJobRunnableMock.getTasks()).thenReturn(nextExpectedTasks);
        final Job finished = jobStarted.finished();
        final Job nextFinished = nextJobStarted.finished();
        when(jobStorage.updateJob(finished)).thenReturn(finished);
        when(jobStorage.updateJob(nextFinished)).thenReturn(nextFinished);
        jobManager.start(job);

        // Then
        verify(jobStorage, times(4)).updateJob(jobArgumentCaptor.capture());
        verify(jobRunnableMock, times(1)).run(jobStarted.getId());
        verify(jobRunnableMock, times(1)).initializeTasks();
        verify(nextJobRunnableMock, times(1)).run(nextJobStarted.getId());
        verify(nextJobRunnableMock, times(1)).initializeTasks();
        final List<Job> captorAllValues = jobArgumentCaptor.getAllValues();
        assertThat(captorAllValues.get(0)).isEqualTo(jobStarted);
        assertThat(captorAllValues.get(0).getTasks()).isEqualTo(expectedTasks);
        assertThat(captorAllValues.get(1).getEndDate()).isNotNull();
        assertThat(captorAllValues.get(1).getTasks()).isEqualTo(expectedTasks);
        assertThat(captorAllValues.get(2)).isEqualTo(nextJobStarted);
        assertThat(captorAllValues.get(2).getTasks()).isEqualTo(nextExpectedTasks);
        assertThat(captorAllValues.get(3).getEndDate()).isNotNull();
        assertThat(captorAllValues.get(3).getTasks()).isEqualTo(nextExpectedTasks);

    }


    @Test
    void should_restart_failed_jobs() throws SymeoException {
        // Given
        final Executor executor = Runnable::run;
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobManager jobManager = new JobManager(executor, jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final UUID teamId = UUID.randomUUID();
        final JobRunnable failedJobRunnable1 = mock(JobRunnable.class);
        final JobRunnable failedJobRunnable2 = mock(JobRunnable.class);

        // When
        jobManager.restartFailedJobs(List.of(
                Job.builder()
                        .id(1L)
                        .code(faker.dragonBall().character() + "-4")
                        .status(Job.FAILED)
                        .organizationId(organizationId)
                        .teamId(teamId)
                        .jobRunnable(failedJobRunnable1)
                        .tasks(List.of())
                        .build(),
                Job.builder()
                        .id(2L)
                        .code(faker.dragonBall().character() + "-4")
                        .status(Job.FAILED)
                        .organizationId(organizationId)
                        .teamId(teamId)
                        .jobRunnable(failedJobRunnable2)
                        .tasks(List.of())
                        .build()
        ));

        // Then
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobStorage, times(4)).updateJob(jobArgumentCaptor.capture());
        verify(failedJobRunnable1, times(1)).run(1L);
        verify(failedJobRunnable2, times(1)).run(2L);
        assertThat(jobArgumentCaptor.getAllValues().get(0).getStatus()).isEqualTo(Job.RESTARTED);
        assertThat(jobArgumentCaptor.getAllValues().get(2).getStatus()).isEqualTo(Job.RESTARTED);
        assertThat(jobArgumentCaptor.getAllValues().get(1).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(jobArgumentCaptor.getAllValues().get(3).getStatus()).isEqualTo(Job.FINISHED);
    }
}
