package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;

import java.util.List;

public interface ExpositionStorageAdapter {
    void savePullRequestDetails(List<PullRequest> pullRequests);

    void savePullRequestHistograms(List<PullRequestHistogram> pullRequestHistograms);

    PullRequestHistogram readPullRequestHistogram(String organizationId, String teamName, String histogramType);

    void saveRepositories(List<Repository> repositories);

    List<Repository> readRepositoriesForOrganization(Organization organization);
}
