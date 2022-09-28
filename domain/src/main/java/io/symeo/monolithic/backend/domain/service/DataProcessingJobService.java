package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobManager;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForOrganizationAndTeamJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForOrganizationJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.InitializeOrganizationSettingsJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
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
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private final RepositoryService repositoryService;
    private final JobManager jobManager;
    private final SymeoJobApiAdapter symeoJobApiAdapter;
    private final OrganizationSettingsService organizationSettingsService;
    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final JobStorage jobStorage;

    @Override
    public void startToCollectRepositoriesForOrganizationId(final UUID organizationId) throws SymeoException {
        jobManager.start(
                Job.builder()
                        .organizationId(organizationId)
                        .jobRunnable(
                                CollectRepositoriesJobRunnable.builder()
                                        .vcsService(vcsService)
                                        .organizationId(organizationId)
                                        .organizationStorageAdapter(organizationStorageAdapter)
                                        .jobStorage(jobStorage)
                                        .build()
                        )
                        .build()
        );
    }

    @Override
    public void startToCollectVcsDataForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
        jobManager.start(
                Job.builder()
                        .jobRunnable(CollectVcsDataForOrganizationAndTeamJobRunnable.builder()
                                .organizationId(organizationId)
                                .teamId(teamId)
                                .expositionStorageAdapter(expositionStorageAdapter)
                                .organizationStorageAdapter(organizationStorageAdapter)
                                .vcsService(vcsService)
                                .jobStorage(jobStorage)
                                .build())
                        .organizationId(organizationId)
                        .nextJob(getInitializeOrganizationSettingsJob(organizationId))
                        .teamId(teamId)
                        .build());
    }

    @Override
    public void startToCollectVcsDataForOrganizationId(UUID organizationId) throws SymeoException {
        jobManager.start(
                Job.builder()
                        .organizationId(organizationId)
                        .jobRunnable(
                                CollectRepositoriesJobRunnable.builder()
                                        .vcsService(vcsService)
                                        .organizationId(organizationId)
                                        .organizationStorageAdapter(organizationStorageAdapter)
                                        .jobStorage(jobStorage)
                                        .build()
                        )
                        .nextJob(
                                Job.builder()
                                        .jobRunnable(CollectVcsDataForOrganizationJobRunnable.builder()
                                                .organizationId(organizationId)
                                                .expositionStorageAdapter(expositionStorageAdapter)
                                                .organizationStorageAdapter(organizationStorageAdapter)
                                                .vcsService(vcsService)
                                                .jobStorage(jobStorage)
                                                .build())
                                        .organizationId(organizationId)
                                        .nextJob(getInitializeOrganizationSettingsJob(organizationId))
                                        .build())
                        .build());
    }

    @Override
    public void startAll() throws SymeoException {
        final List<Organization> organizations = organizationStorageAdapter.findAllOrganization();
        for (Organization organization : organizations) {
            symeoJobApiAdapter.startJobForOrganizationId(organization.getId());
        }
    }


    private Job getInitializeOrganizationSettingsJob(final UUID organizationId) {
        return Job.builder()
                .jobRunnable(
                        InitializeOrganizationSettingsJobRunnable.builder()
                                .organizationSettingsService(organizationSettingsService)
                                .organizationStorageAdapter(organizationStorageAdapter)
                                .organizationId(organizationId)
                                .jobStorage(jobStorage)
                                .build()
                )
                .organizationId(organizationId)
                .build();
    }

}
