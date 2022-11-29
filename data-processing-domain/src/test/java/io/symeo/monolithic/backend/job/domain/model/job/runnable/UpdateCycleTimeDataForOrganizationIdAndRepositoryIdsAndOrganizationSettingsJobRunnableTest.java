package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.task.RepositoriesDateRangeTask;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnableTest {

    private static final Faker faker = new Faker();

    @Test
    void should_initialize_all_tasks() throws SymeoException {
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
        final UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable =
                UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.builder()
                        .repositories(repositories)
                        .vcsDataProcessingService(vcsDataProcessingService)
                        .dataProcessingJobStorage(dataProcessingJobStorage)
                        .deployDetectionType(faker.name().name())
                        .excludeBranchRegexes(List.of())
                        .build();

        // When
        updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.initializeTasks();

        // Then
        verifyNoInteractions(dataProcessingJobStorage);
        verifyNoInteractions(vcsDataProcessingService);
        final List<Task> tasks = updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.getTasks();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getStatus()).isEqualTo("TO_DO");
        assertThat(tasks.get(0).getInput()).isInstanceOf(RepositoriesDateRangeTask.class);
        final RepositoriesDateRangeTask repositoriesDateRangeTask = (RepositoriesDateRangeTask) tasks.get(0).getInput();
        assertThat(repositoriesDateRangeTask.getRepositories()).isEqualTo(repositories);
    }

    @Test
    void should_execute_all_tasks() throws SymeoException {
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

        final String deployDetectionType = faker.rickAndMorty().character();
        final String pullRequestMergedOnBranchRegex = faker.name().name();
        final String tagRegex = faker.gameOfThrones().character();
        final List<String> excludedBranchRegex = List.of("main", "staging");

        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final DataProcessingJobStorage dataProcessingJobStorage = mock(DataProcessingJobStorage.class);
        final UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable =
                UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.builder()
                        .repositories(repositories)
                        .deployDetectionType(deployDetectionType)
                        .pullRequestMergedOnBranchRegexes(pullRequestMergedOnBranchRegex)
                        .tagRegex(tagRegex)
                        .excludeBranchRegexes(excludedBranchRegex)
                        .vcsDataProcessingService(vcsDataProcessingService)
                        .dataProcessingJobStorage(dataProcessingJobStorage)
                        .build();
        final long jobId = faker.number().randomNumber();

        // When
        updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.initializeTasks();
        updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.run(jobId);

        // Then
        final List<Task> tasks = updateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.getTasks();
        final int numberOfTasks = 1;
        assertThat(tasks.size()).isEqualTo(numberOfTasks);
        assertThat(tasks.get(0).getStatus()).isEqualTo("DONE");
        assertThat(tasks.get(0).getInput()).isInstanceOf(RepositoriesDateRangeTask.class);
        final RepositoriesDateRangeTask repositoriesDateRangeTask = (RepositoriesDateRangeTask) tasks.get(0).getInput();
        for (Repository repository : repositoriesDateRangeTask.getRepositories()) {
            verify(vcsDataProcessingService, times(1))
                    .updateCycleTimeDataForRepositoryAndDateRange(
                            repository, deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludedBranchRegex
                    );
        }
        verify(dataProcessingJobStorage, times(numberOfTasks)).updateJobWithTasksForJobId(jobId, tasks);

    }

    @Test
    void should_return_job_code() {
        // Then
        assertThat(UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable.JOB_CODE)
                .isEqualTo("UPDATE_CYCLE_TIME_DATA_FOR_REPOSITORY_IDS_AND_DATE_RANGES_JOB");
    }
}
