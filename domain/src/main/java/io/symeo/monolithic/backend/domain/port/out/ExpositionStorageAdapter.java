package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ExpositionStorageAdapter {
    void savePullRequestDetails(List<PullRequest> pullRequests);

    void saveRepositories(List<Repository> repositories);

    List<Repository> readRepositoriesForOrganization(Organization organization);

    List<PullRequest> findAllPullRequestsForOrganizationAndTeamId(Organization organization, UUID teamId) throws SymeoException;

    List<PullRequestView> readPullRequestsTimeToMergeViewForOrganizationAndTeam(Organization organization,
                                                                                UUID teamId) throws SymeoException;

    List<PullRequestView> readPullRequestsSizeViewForOrganizationAndTeam(Organization organization, UUID teamId)
            throws SymeoException;

    List<PullRequestView> readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(UUID teamId,
                                                                                                 Date startDate,
                                                                                                 Date endDate,
                                                                                                 int pageIndex, int pageSize,
                                                                                                 String sortingParameter,
                                                                                                 String sortingDirection)
            throws SymeoException;

    int countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(UUID teamId, Date startDate, Date endDate)
            throws SymeoException;
}
