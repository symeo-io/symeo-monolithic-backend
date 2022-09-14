package io.symeo.monolithic.backend.domain.job;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_return_code_from_job_runnable() {
        // Given
        final String jobCode = faker.ancient().god();
        final Job job = Job.builder()
                .organizationId(UUID.randomUUID())
                .tasks(List.of())
                .jobRunnable(new JobRunnable() {
                    @Override
                    public void run(List<Task> tasks) {

                    }

                    @Override
                    public String getCode() {
                        return jobCode;
                    }

                    @Override
                    public List<Task> getTasks() {
                        return null;
                    }

                }).build();

        // When
        final String code = job.getCode();

        // Then
        assertThat(code).isEqualTo(jobCode);
    }

    @Test
    void should_return_code_from_code_attribute() {
        // Given
        final String expectedCode = faker.name().firstName();
        final Job job = Job.builder()
                .organizationId(UUID.randomUUID())
                .tasks(List.of())
                .code(expectedCode)
                .build();


        // When
        final String code = job.getCode();

        // Then
        assertThat(code).isEqualTo(expectedCode);

    }

    @Test
    void should_return_finished_job() {
        // Given
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        final Job job = Job.builder()
                .organizationId(UUID.randomUUID())
                .tasks(List.of())
                .code(faker.rickAndMorty().character())
                .jobRunnable(jobRunnable)
                .build();
        final List<Task> expectedTasks = List.of(
                Task.builder().input(faker.name().firstName()).build(),
                Task.builder().input(faker.name().firstName()).build()
        );

        // When
        when(jobRunnable.getTasks()).thenReturn(expectedTasks);
        final Job finishedJob = job.finished();

        // Then
        assertThat(finishedJob.getTasks()).isEqualTo(expectedTasks);
        assertThat(finishedJob.getStatus()).isEqualTo(Job.FINISHED);
    }

    @Test
    void should_return_failed_job() {
        // Given
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        final Job job = Job.builder()
                .organizationId(UUID.randomUUID())
                .tasks(List.of())
                .code(faker.rickAndMorty().character())
                .jobRunnable(jobRunnable)
                .build();
        final List<Task> expectedTasks = List.of(
                Task.builder().input(faker.name().firstName()).build(),
                Task.builder().input(faker.name().firstName()).build()
        );
        final SymeoException expectedSymeoException =
                SymeoException.builder().message(faker.name().firstName()).code(faker.dragonBall().character()).build();

        // When
        when(jobRunnable.getTasks()).thenReturn(expectedTasks);
        final Job failedJob =
                job.failed(expectedSymeoException);

        // Then
        assertThat(failedJob.getTasks()).isEqualTo(expectedTasks);
        assertThat(failedJob.getStatus()).isEqualTo(Job.FAILED);
        assertThat(failedJob.getError()).isEqualTo(expectedSymeoException.toString());
    }


}
