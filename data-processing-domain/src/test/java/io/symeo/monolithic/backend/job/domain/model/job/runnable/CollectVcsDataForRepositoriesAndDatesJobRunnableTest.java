package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.task.RepositoryDateRangeTask;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CollectVcsDataForRepositoriesAndDatesJobRunnableTest {

    private static final Faker faker = new Faker();

    @Test
    void should_initialize_all_tasks_for_two_years() {
        // Given
        final List<Repository> repositories = List.of(
                Repository.builder()
                        .name(faker.name().firstName() + "-1")
                        .vcsOrganizationName(faker.rickAndMorty().location() + "-1")
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character() + "-1")
                        .id(faker.idNumber().invalid() + "-1")
                        .build(),
                Repository.builder()
                        .name(faker.name().firstName() + "-2")
                        .vcsOrganizationName(faker.rickAndMorty().location() + "-2")
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character() + "-2")
                        .id(faker.idNumber().invalid() + "-2")
                        .build()
        );
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final DataProcessingJobStorage dataProcessingJobStorage = mock(DataProcessingJobStorage.class);
        final CollectVcsDataForRepositoriesAndDatesJobRunnable collectVcsDataForRepositoriesAndDatesJobRunnable =
                CollectVcsDataForRepositoriesAndDatesJobRunnable.builder()
                        .repositories(repositories)
                        .vcsDataProcessingService(vcsDataProcessingService)
                        .dataProcessingJobStorage(dataProcessingJobStorage)
                        .build();

        // When
        collectVcsDataForRepositoriesAndDatesJobRunnable.initializeTasks();

        // Then
        verifyNoInteractions(dataProcessingJobStorage);
        verifyNoInteractions(vcsDataProcessingService);
        final List<Task> tasks = collectVcsDataForRepositoriesAndDatesJobRunnable.getTasks();
        assertThat(tasks).hasSize(repositories.size() * 720 / 30);
        for (Task task : tasks) {
            assertThat(task.getStatus()).isEqualTo("TO_DO");
            assertThat(task.getInput()).isInstanceOf(RepositoryDateRangeTask.class);
            final RepositoryDateRangeTask repositoryDateRangeTask = (RepositoryDateRangeTask) task.getInput();
            assertThat(repositoryDateRangeTask.getRepository()).isIn(repositories);
        }
    }

    @Test
    void should_execute_all_tasks_for_two_years() throws SymeoException {
        // Given
        final List<Repository> repositories = List.of(
                Repository.builder()
                        .name(faker.name().firstName() + "-1")
                        .vcsOrganizationName(faker.rickAndMorty().location() + "-1")
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character() + "-1")
                        .id(faker.idNumber().invalid() + "-1")
                        .build(),
                Repository.builder()
                        .name(faker.name().firstName() + "-2")
                        .vcsOrganizationName(faker.rickAndMorty().location() + "-2")
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character() + "-2")
                        .id(faker.idNumber().invalid() + "-2")
                        .build()
        );
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final DataProcessingJobStorage dataProcessingJobStorage = mock(DataProcessingJobStorage.class);
        final CollectVcsDataForRepositoriesAndDatesJobRunnable collectVcsDataForRepositoriesAndDatesJobRunnable =
                CollectVcsDataForRepositoriesAndDatesJobRunnable.builder()
                        .repositories(repositories)
                        .vcsDataProcessingService(vcsDataProcessingService)
                        .dataProcessingJobStorage(dataProcessingJobStorage)
                        .build();
        final long jobId = faker.number().randomNumber();

        // When
        collectVcsDataForRepositoriesAndDatesJobRunnable.initializeTasks();
        collectVcsDataForRepositoriesAndDatesJobRunnable.run(jobId);

        // Then
        final List<Task> tasks = collectVcsDataForRepositoriesAndDatesJobRunnable.getTasks();
        final int numberOfTasks = repositories.size() * 720 / 30;
        assertThat(tasks).hasSize(numberOfTasks);
        for (Task task : tasks) {
            assertThat(task.getStatus()).isEqualTo("DONE");
            assertThat(task.getInput()).isInstanceOf(RepositoryDateRangeTask.class);
            final RepositoryDateRangeTask repositoryDateRangeTask = (RepositoryDateRangeTask) task.getInput();
            assertThat(repositoryDateRangeTask.getRepository()).isIn(repositories);
            verify(vcsDataProcessingService, times(1))
                    .collectVcsDataForRepositoryAndDateRange(repositoryDateRangeTask.getRepository(),
                            repositoryDateRangeTask.getStartDate(), repositoryDateRangeTask.getEndDate());
        }
        verify(dataProcessingJobStorage, times(numberOfTasks)).updateJobWithTasksForJobId(any(), any());
    }


    @Test
    void should_return_job_code() {
        // Then
        assertThat(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE)
                .isEqualTo("COLLECT_VCS_DATA_FOR_REPOSITORY_IDS_AND_DATE_RANGES_JOB");
    }
}
