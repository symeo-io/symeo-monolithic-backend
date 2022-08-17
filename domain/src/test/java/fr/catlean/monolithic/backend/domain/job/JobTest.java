package fr.catlean.monolithic.backend.domain.job;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JobTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_return_code_from_job_runnable() {
        // Given
        final String jobCode = faker.ancient().god();
        final Job job = Job.builder()
                .organizationId(UUID.randomUUID())
                .jobRunnable(new JobRunnable() {
                    @Override
                    public void run() throws CatleanException {

                    }

                    @Override
                    public String getCode() {
                        return jobCode;
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
                .code(expectedCode)
                .build();


        // When
        final String code = job.getCode();

        // Then
        assertThat(code).isEqualTo(expectedCode);

    }
}
