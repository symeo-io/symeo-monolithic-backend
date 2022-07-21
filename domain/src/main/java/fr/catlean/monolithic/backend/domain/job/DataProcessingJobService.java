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

import java.util.List;

@AllArgsConstructor
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final VcsService vcsService;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final PullRequesHistogramService pullRequesHistogramService;
    private final RepositoryService repositoryService;

    @Override
    public void start(final String vcsOrganizationName) throws CatleanException {
        final Organization organization =
                accountOrganizationStorageAdapter.findVcsOrganizationForName(vcsOrganizationName);
        collectRepositories(organization);
        collectPullRequests(organization);
    }

    private void collectPullRequests(Organization organization) throws CatleanException {
        final List<PullRequest> pullRequestList =
                vcsService.collectPullRequestsForOrganization(organization);
        pullRequesHistogramService.savePullRequests(pullRequestList);
        pullRequesHistogramService.computeAndSavePullRequestSizeHistogram(pullRequestList, organization);
        pullRequesHistogramService.computeAndSavePullRequestTimeHistogram(pullRequestList, organization);
    }

    private void collectRepositories(Organization organization) throws CatleanException {
        List<Repository> repositories = vcsService.collectRepositoriesForOrganization(organization);
        repositories =
                repositories.stream().map(repository -> repository.toBuilder().organization(organization).build()).toList();
        repositoryService.saveRepositories(repositories);
    }
}
