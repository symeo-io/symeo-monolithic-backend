package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CollectRepositoriesJobRunnableTest {

    private final static Faker faker = new Faker();

    @Test
    void should_initialize_tasks_given_a_vcs_organization() {
        // Given
        final VcsOrganization vcsOrganization = VcsOrganization.builder()
                .externalId(faker.pokemon().name())
                .organizationId(UUID.randomUUID())
                .id(faker.number().randomNumber())
                .vcsId(faker.pokemon().location())
                .name(faker.robin().quote())
                .build();
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final DataProcessingJobStorage dataProcessingJobStorage = mock(DataProcessingJobStorage.class);
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable = CollectRepositoriesJobRunnable.builder()
                .vcsOrganization(vcsOrganization)
                .dataProcessingJobStorage(dataProcessingJobStorage)
                .vcsDataProcessingService(vcsDataProcessingService)
                .build();

        // When
        collectRepositoriesJobRunnable.initializeTasks();

        // Then
        verifyNoInteractions(dataProcessingJobStorage);
        verifyNoInteractions(vcsDataProcessingService);
        final List<Task> tasks = collectRepositoriesJobRunnable.getTasks();
        assertThat(tasks).hasSize(1);
        final Task task = tasks.get(0);
        assertThat(task.getStatus()).isEqualTo("TO_DO");
        assertThat((VcsOrganization) task.getInput()).isEqualTo(vcsOrganization);
    }


    @Test
    void should_execute_all_tasks_given_a_vcs_organization() throws SymeoException {
        // Given
        final VcsOrganization vcsOrganization = VcsOrganization.builder()
                .externalId(faker.pokemon().name())
                .organizationId(UUID.randomUUID())
                .id(faker.number().randomNumber())
                .vcsId(faker.pokemon().location())
                .name(faker.robin().quote())
                .build();
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final DataProcessingJobStorage dataProcessingJobStorage = mock(DataProcessingJobStorage.class);
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable = CollectRepositoriesJobRunnable.builder()
                .vcsOrganization(vcsOrganization)
                .dataProcessingJobStorage(dataProcessingJobStorage)
                .vcsDataProcessingService(vcsDataProcessingService)
                .build();
        final long jobId = faker.number().randomNumber();

        // When
        collectRepositoriesJobRunnable.initializeTasks();
        collectRepositoriesJobRunnable.run(jobId);

        // Then
        final List<Task> tasks = collectRepositoriesJobRunnable.getTasks();
        assertThat(tasks).hasSize(1);
        verify(vcsDataProcessingService, times(1))
                .collectRepositoriesForVcsOrganization(vcsOrganization);
        verify(dataProcessingJobStorage, times(1))
                .updateJobWithTasksForJobId(jobId, tasks);
        assertThat(tasks.get(0).getStatus()).isEqualTo("DONE");
    }

    @Test
    void should_return_job_code() {
        assertThat(CollectRepositoriesJobRunnable.JOB_CODE)
                .isEqualTo("COLLECT_REPOSITORIES_FOR_VCS_ORGANIZATION_JOB");
    }
}
