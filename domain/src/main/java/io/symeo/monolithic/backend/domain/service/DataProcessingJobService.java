package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobManager;
import io.symeo.monolithic.backend.domain.job.runnable.CollectPullRequestsJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final VcsService vcsService;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final RepositoryService repositoryService;
    private final JobManager jobManager;

    @Override
    public void start(final String vcsOrganizationName) throws SymeoException {
        LOGGER.info("Starting to collect data for organization {}", vcsOrganizationName);
        final Organization organization =
                accountOrganizationStorageAdapter.findVcsOrganizationForName(vcsOrganizationName);
        collectRepositories(organization);
    }

    private void collectRepositories(Organization organization) throws SymeoException {
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

    private Job getCollectPullRequestsJob(Organization organization) {
        return
                Job.builder()
                        .jobRunnable(CollectPullRequestsJobRunnable.builder()
                                .organization(organization)
                                .vcsService(vcsService)
                                .build())
                        .organizationId(organization.getId())
                        .build();


    }

}
