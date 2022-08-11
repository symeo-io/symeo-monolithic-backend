package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestSizeView;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;

import java.util.List;
import java.util.UUID;

public interface ExpositionStorageAdapter {
    void savePullRequestDetails(List<PullRequest> pullRequests);

    void saveRepositories(List<Repository> repositories);

    List<Repository> readRepositoriesForOrganization(Organization organization);

    List<PullRequest> findAllPullRequestsForOrganizationAndTeamId(Organization organization, UUID teamId) throws CatleanException;

    List<PullRequestTimeToMergeView> readPullRequestsTimeToMergeViewForOrganizationAndTeam(Organization organization,
                                                                                           UUID teamId) throws CatleanException;

    List<PullRequestSizeView> readPullRequestsSizeViewForOrganizationAndTeam(Organization organization, UUID teamId)
            throws CatleanException;
}
