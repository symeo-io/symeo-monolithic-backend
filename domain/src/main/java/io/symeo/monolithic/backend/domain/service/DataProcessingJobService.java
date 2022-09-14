package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobManager;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.InitializeOrganizationSettingsJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.SymeoJobApiAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final VcsService vcsService;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final RepositoryService repositoryService;
    private final JobManager jobManager;
    private final SymeoJobApiAdapter symeoJobApiAdapter;
    private final OrganizationSettingsService organizationSettingsService;
    private final ExpositionStorageAdapter expositionStorageAdapter;

    @Override
    public void startToCollectRepositoriesForOrganizationId(final UUID organizationId) throws SymeoException {
        final Organization organization =
                accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        collectRepositoriesForOrganization(organization);
    }

    @Override
    public void startToCollectVcsDataForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
        final Organization organization =
                accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        final List<Repository> repositories =
                populateRepositoriesWithOrganizationId(
                        expositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organizationId, teamId),
                        organizationId);
        collectVcsDataForRepositoriesAndOrganization(organization, repositories);
    }

    @Override
    public void startToCollectVcsDataForOrganizationId(UUID organizationId) throws SymeoException {
        final Organization organization =
                accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        final List<Repository> repositories =
                populateRepositoriesWithOrganizationId(
                        expositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(
                                organization.getId()), organizationId);
        collectVcsDataForRepositoriesAndOrganization(organization, repositories);
    }

    @Override
    public void startAll() throws SymeoException {
        final List<Organization> organizations = accountOrganizationStorageAdapter.findAllOrganization();
        organizations.forEach(organization -> symeoJobApiAdapter.startJobForOrganizationId(organization.getId()));
    }

    private void collectRepositoriesForOrganization(final Organization organization) throws SymeoException {
        jobManager.start(
                Job.builder()
                        .organizationId(organization.getId())
                        .jobRunnable(
                                CollectRepositoriesJobRunnable.builder()
                                        .vcsService(vcsService)
                                        .repositoryService(repositoryService)
                                        .build()
                        )
                        .tasks(
                                List.of(
                                        Task.builder()
                                                .input(organization)
                                                .build()
                                )
                        )
                        .build()
        );
    }

    private void collectVcsDataForRepositoriesAndOrganization(final Organization organization,
                                                              final List<Repository> repositories) throws SymeoException {
        jobManager.start(
                Job.builder()
                        .jobRunnable(CollectVcsDataForRepositoriesJobRunnable.builder()
                                .organization(organization)
                                .vcsService(vcsService)
                                .build())
                        .organizationId(organization.getId())
                        .nextJob(getInitializeOrganizationSettingsJob(organization))
                        .tasks(
                                repositories.stream()
                                        .map(repository -> Task.builder().input(repository).build())
                                        .toList()
                        )
                        .build()
        );
    }


    private Job getInitializeOrganizationSettingsJob(final Organization organization) {
        return Job.builder()
                .jobRunnable(
                        InitializeOrganizationSettingsJobRunnable.builder()
                                .organizationSettingsService(organizationSettingsService)
                                .build()
                )
                .tasks(
                        List.of(
                                Task.builder()
                                        .input(organization)
                                        .build()
                        )
                )
                .organizationId(organization.getId())
                .build();
    }


    private List<Repository> populateRepositoriesWithOrganizationId(final List<Repository> repositories,
                                                                    final UUID organizationId) {
        return repositories.stream()
                .map(repository -> repository.toBuilder().organizationId(organizationId).build())
                .toList();
    }
}
