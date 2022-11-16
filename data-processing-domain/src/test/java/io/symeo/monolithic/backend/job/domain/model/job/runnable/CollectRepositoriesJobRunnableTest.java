package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.organization.OrganizationSettingsView;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
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
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter =
                mock(DataProcessingExpositionStorageAdapter.class);
        final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter =
                mock(AutoSymeoDataProcessingJobApiAdapter.class);
        final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter = mock(VcsOrganizationStorageAdapter.class);
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable = CollectRepositoriesJobRunnable.builder()
                .vcsOrganization(vcsOrganization)
                .vcsOrganizationStorageAdapter(vcsOrganizationStorageAdapter)
                .dataProcessingJobStorage(dataProcessingJobStorage)
                .vcsDataProcessingService(vcsDataProcessingService)
                .autoSymeoDataProcessingJobApiAdapter(autoSymeoDataProcessingJobApiAdapter)
                .dataProcessingExpositionStorageAdapter(dataProcessingExpositionStorageAdapter)
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
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter =
                mock(DataProcessingExpositionStorageAdapter.class);
        final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter =
                mock(AutoSymeoDataProcessingJobApiAdapter.class);
        final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter = mock(VcsOrganizationStorageAdapter.class);
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable = CollectRepositoriesJobRunnable.builder()
                .vcsOrganization(vcsOrganization)
                .vcsOrganizationStorageAdapter(vcsOrganizationStorageAdapter)
                .dataProcessingJobStorage(dataProcessingJobStorage)
                .vcsDataProcessingService(vcsDataProcessingService)
                .autoSymeoDataProcessingJobApiAdapter(autoSymeoDataProcessingJobApiAdapter)
                .dataProcessingExpositionStorageAdapter(dataProcessingExpositionStorageAdapter)
                .build();
        final long jobId = faker.number().randomNumber();

        // When
        when(dataProcessingExpositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(vcsOrganization.getOrganizationId()))
                .thenReturn(List.of());
        collectRepositoriesJobRunnable.initializeTasks();
        collectRepositoriesJobRunnable.run(jobId);

        // Then
        final List<Task> tasks = collectRepositoriesJobRunnable.getTasks();
        assertThat(tasks).hasSize(1);
        verify(vcsDataProcessingService, times(1))
                .collectRepositoriesForVcsOrganization(vcsOrganization);
        verify(dataProcessingJobStorage, times(1))
                .updateJobWithTasksForJobId(jobId, tasks);
        verifyNoInteractions(autoSymeoDataProcessingJobApiAdapter);
        assertThat(tasks.get(0).getStatus()).isEqualTo("DONE");
    }

    @Test
    void should_execute_all_tasks_and_start_job_for_repositories() throws SymeoException {
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
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter =
                mock(DataProcessingExpositionStorageAdapter.class);
        final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter =
                mock(AutoSymeoDataProcessingJobApiAdapter.class);
        final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter = mock(VcsOrganizationStorageAdapter.class);
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable = CollectRepositoriesJobRunnable.builder()
                .vcsOrganization(vcsOrganization)
                .vcsOrganizationStorageAdapter(vcsOrganizationStorageAdapter)
                .dataProcessingJobStorage(dataProcessingJobStorage)
                .vcsDataProcessingService(vcsDataProcessingService)
                .autoSymeoDataProcessingJobApiAdapter(autoSymeoDataProcessingJobApiAdapter)
                .dataProcessingExpositionStorageAdapter(dataProcessingExpositionStorageAdapter)
                .build();
        final long jobId = faker.number().randomNumber();
        final List<Repository> repositoryList = List.of(Repository.builder()
                .organizationId(vcsOrganization.getOrganizationId())
                .vcsOrganizationName(vcsOrganization.getName())
                .vcsOrganizationId(vcsOrganization.getVcsId())
                .name(faker.rickAndMorty().character())
                .id(faker.lordOfTheRings().character())
                .build());
        final OrganizationSettingsView organizationSettingsView = OrganizationSettingsView.builder()
                .deployDetectionType(faker.rickAndMorty().character())
                .excludeBranchRegexes(List.of())
                .tagRegex(faker.gameOfThrones().character())
                .deployDetectionType(faker.name().firstName())
                .build();

        // When
        collectRepositoriesJobRunnable.initializeTasks();
        when(dataProcessingExpositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(vcsOrganization.getOrganizationId()))
                .thenReturn(repositoryList);
        when(vcsOrganizationStorageAdapter.findOrganizationSettingsViewForOrganizationId(vcsOrganization.getOrganizationId()))
                .thenReturn(organizationSettingsView);
        collectRepositoriesJobRunnable.run(jobId);

        // Then
        verify(autoSymeoDataProcessingJobApiAdapter, times(1))
                .autoStartDataProcessingJobForOrganizationIdAndRepositoryIds(
                        vcsOrganization.getOrganizationId(), repositoryList.stream().map(Repository::getId).toList(),
                        organizationSettingsView.getDeployDetectionType(),
                        organizationSettingsView.getPullRequestMergedOnBranchRegex(),
                        organizationSettingsView.getTagRegex(), organizationSettingsView.getExcludeBranchRegexes()
                );
    }

    @Test
    void should_return_job_code() {
        assertThat(CollectRepositoriesJobRunnable.JOB_CODE)
                .isEqualTo("COLLECT_REPOSITORIES_FOR_VCS_ORGANIZATION_JOB");
    }
}
