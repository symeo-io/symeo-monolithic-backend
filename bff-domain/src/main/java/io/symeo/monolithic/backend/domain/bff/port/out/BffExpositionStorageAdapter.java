package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface BffExpositionStorageAdapter {

    List<CommitView> readAllCommitsForTeamId(UUID teamId) throws SymeoException;

    List<RepositoryView> readRepositoriesForOrganization(Organization organization);

    List<PullRequestView> readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(Organization organization,
                                                                                                          UUID teamId,
                                                                                                          Date startDate,
                                                                                                          Date endDate) throws SymeoException;

    List<PullRequestView> readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(Organization organization, UUID teamId,
                                                                                                  Date startDate,
                                                                                                  Date endDate)
            throws SymeoException;


    List<PullRequestView> readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(UUID teamId,
                                                                                                 Date startDate,
                                                                                                 Date endDate,
                                                                                                 int pageIndex,
                                                                                                 int pageSize,
                                                                                                 String sortingParameter,
                                                                                                 String sortingDirection)
            throws SymeoException;

    List<PullRequestView> findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(UUID teamId,
                                                                                       Date startDate,
                                                                                       Date endDate,
                                                                                       int pageIndex,
                                                                                       int pageSize,
                                                                                       String sortingParameter,
                                                                                       String sortingDirection)
            throws SymeoException;

    int countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(UUID teamId, Date startDate, Date endDate)
            throws SymeoException;

    List<PullRequestView> readPullRequestsWithCommitsForTeamIdUntilEndDate(UUID teamId,
                                                                           Date endDate)
            throws SymeoException;

    String findDefaultMostUsedBranchForOrganizationId(UUID organizationId) throws SymeoException;


    List<RepositoryView> findAllRepositoriesForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException;

    List<PullRequestView> readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(UUID teamId, Date startDate,
                                                                                    Date endDate) throws SymeoException;

    List<PullRequestView> readMergedPullRequestsForTeamIdUntilEndDate(UUID teamId, Date endDate) throws SymeoException;

    List<TagView> findTagsForTeamId(UUID teamId) throws SymeoException;

    List<CommitView> readCommitsMatchingShaListBetweenStartDateAndEndDate(List<String> shaList, Date startDate,
                                                                          Date endDate) throws SymeoException;
}
