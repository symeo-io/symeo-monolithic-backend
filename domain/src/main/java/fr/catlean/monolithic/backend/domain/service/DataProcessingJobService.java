package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.job.JobManager;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectPullRequestsJobRunnable;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final VcsService vcsService;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final PullRequestHistogramService pullRequestHistogramService;
    private final RepositoryService repositoryService;
    private final JobManager jobManager;

    @Override
    public void start(final String vcsOrganizationName) throws CatleanException {
        LOGGER.info("Starting to collect data for organization {}", vcsOrganizationName);
        final Organization organization =
                accountOrganizationStorageAdapter.findVcsOrganizationForName(vcsOrganizationName);
        collectRepositories(organization);
    }

    private void collectRepositories(Organization organization) throws CatleanException {
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
                                .pullRequestHistogramService(pullRequestHistogramService)
                                .build())
                        .organizationId(organization.getId())
                        .build();


    }

}
