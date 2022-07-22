package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.job.JobRunnable;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresJobAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;
import java.util.UUID;

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
    void should_save_and_update_job() throws CatleanException {
        // Given
        final PostgresJobAdapter postgresJobAdapter = new PostgresJobAdapter(jobRepository);
        final Job job = Job.builder()
                .organizationId(UUID.randomUUID())
                .jobRunnable(new JobRunnable() {
                    @Override
                    public void run() {

                    }

                    @Override
                    public String getCode() {
                        return faker.dragonBall().character();
                    }
                })
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
}
