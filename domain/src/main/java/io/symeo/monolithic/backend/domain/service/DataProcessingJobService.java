package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobManager;
import io.symeo.monolithic.backend.domain.job.runnable.CollectPullRequestsJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.InitializeOrganizationSettingsJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
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

    @Override
    public void start(final UUID organizationId) throws SymeoException {
        LOGGER.info("Starting to collect data for organization {}", organizationId);
        final Organization organization =
                accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        collectRepositories(organization);
    }

    @Override
    public void startAll() throws SymeoException {
        final List<Organization> organizations = accountOrganizationStorageAdapter.findAllOrganization();
        organizations.forEach(organization -> symeoJobApiAdapter.startJobForOrganizationId(organization.getId()));
    }

    private void collectRepositories(final Organization organization) throws SymeoException {
        jobManager.start(
                Job.builder()
                        .organizationId(organization.getId())
                        .jobRunnable(
                                CollectRepositoriesJobRunnable.builder()
                                        .organization(organization)
                                        .vcsService(vcsService)
                                        .repositoryService(repositoryService)
                                        .build()
                        )
                        .nextJob(getCollectPullRequestsJob(organization))
                        .build()
        );
    }

    private Job getCollectPullRequestsJob(final Organization organization) {
        return
                Job.builder()
                        .jobRunnable(CollectPullRequestsJobRunnable.builder()
                                .organization(organization)
                                .vcsService(vcsService)
                                .build())
                        .organizationId(organization.getId())
                        .nextJob(getInitializeOrganizationSettingsJob(organization))
                        .build();
    }

    private Job getInitializeOrganizationSettingsJob(final Organization organization) {
        return Job.builder()
                .jobRunnable(
                        InitializeOrganizationSettingsJobRunnable.builder()
                                .organizationSettingsService(organizationSettingsService)
                                .organization(organization)
                                .build()
                )
                .organizationId(organization.getId())
                .build();
    }

}
