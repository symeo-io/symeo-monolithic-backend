package fr.catlean.monolithic.backend.domain.job;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequesHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;

@AllArgsConstructor
@Slf4j
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final VcsService vcsService;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final PullRequesHistogramService pullRequesHistogramService;
    private final RepositoryService repositoryService;
    private final Executor executor;

    @Override
    public void start(final String vcsOrganizationName) throws CatleanException {
        LOGGER.info("Starting to collect data for organization {}", vcsOrganizationName);
        final Organization organization =
                accountOrganizationStorageAdapter.findVcsOrganizationForName(vcsOrganizationName);
        collectRepositories(organization);
        collectPullRequests(organization);
    }

    private void collectPullRequests(Organization organization) {
        executor.execute(
                CollectPullRequestsRunnable.builder()
                        .organization(organization)
                        .vcsService(vcsService)
                        .pullRequesHistogramService(pullRequesHistogramService)
                        .build()
        );
    }

    private void collectRepositories(Organization organization) throws CatleanException {
        List<Repository> repositories = vcsService.collectRepositoriesForOrganization(organization);
        repositories =
                repositories.stream().map(repository -> repository.toBuilder().organization(organization).build()).toList();
        repositoryService.saveRepositories(repositories);
    }


    @AllArgsConstructor
    @Value
    @Builder
    @Slf4j
    private static class CollectPullRequestsRunnable implements Runnable {

        VcsService vcsService;
        Organization organization;
        PullRequesHistogramService pullRequesHistogramService;


        @Override
        public void run() {
            try {
                final List<PullRequest> pullRequestList =
                        vcsService.collectPullRequestsForOrganization(organization);
                pullRequesHistogramService.savePullRequests(pullRequestList);
                pullRequesHistogramService.computeAndSavePullRequestSizeHistogram(pullRequestList, organization);
                pullRequesHistogramService.computeAndSavePullRequestTimeHistogram(pullRequestList, organization);
            } catch (CatleanException e) {
                LOGGER.error("Error while collection PRs for organization {}", organization, e);
            }

        }
    }
}
