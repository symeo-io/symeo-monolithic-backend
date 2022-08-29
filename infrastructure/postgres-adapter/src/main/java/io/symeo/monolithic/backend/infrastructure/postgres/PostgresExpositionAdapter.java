package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.pagination.Pagination;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestCurveMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.buildPagination;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.SortingMapper.directionToPostgresSortingValue;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper.sortingParameterToDatabaseAttribute;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Slf4j
public class PostgresExpositionAdapter implements ExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;
    private final PullRequestSizeRepository pullRequestSizeRepository;
    private final PullRequestFullViewRepository pullRequestFullViewRepository;
    private final CustomPullRequestViewRepository customPullRequestViewRepository;
    private final CommitRepository commitRepository;
    private final CommentRepository commentRepository;

    @Override
    public void savePullRequestDetailsWithLinkedCommitsAndComments(List<PullRequest> pullRequests) {
        pullRequestRepository.saveAll(pullRequests.stream().map(PullRequestMapper::domainToEntity)
                .toList());
        commitRepository.saveAll(pullRequests.stream().map(PullRequestMapper::pullRequestToCommitEntities)
                .flatMap(Collection::stream)
                .toList());
        commentRepository.saveAll(pullRequests.stream().map(PullRequestMapper::pullRequestToCommentEntities)
                .flatMap(Collection::stream)
                .toList());
    }

    @Override
    public void saveRepositories(List<Repository> repositories) {
        repositoryRepository.saveAll(repositories.stream().map(RepositoryMapper::domainToEntity).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Repository> readRepositoriesForOrganization(Organization organization) {
        return repositoryRepository.findRepositoryEntitiesByOrganizationId(organization.getId())
                .stream()
                .map(RepositoryMapper::entityToDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequest> findAllPullRequestsForOrganizationAndTeamId(Organization organization, UUID teamId) throws SymeoException {
        try {
            return ofNullable(teamId)
                    .map(uuid -> pullRequestRepository.findAllByOrganizationIdAndTeamId(organization.getId(), uuid))
                    .orElseGet(() -> pullRequestRepository.findAllByOrganizationId(organization.getId()))
                    .stream().map(PullRequestMapper::entityToDomain).toList();
        } catch (Exception e) {
            LOGGER.error("Failed to find all pull requests for organization {}", organization, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find all pull requests for organization")
                    .build();
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<PullRequestView> readPullRequestsTimeToMergeViewForOrganizationAndTeam(final Organization organization,
                                                                                       final UUID teamId) throws SymeoException {
        try {
            return pullRequestTimeToMergeRepository.findTimeToMergeDTOsByOrganizationIdAndTeamId(
                            organization.getId(), teamId)
                    .stream()
                    .map(PullRequestCurveMapper::dtoToView)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to read all PR time to merge curve for organization {}", organization, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to read all PR time to merge curve for organization")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestView> readPullRequestsSizeViewForOrganizationAndTeam(Organization organization,
                                                                                UUID teamId) throws SymeoException {
        try {
            return pullRequestSizeRepository.findPullRequestSizeDTOsByOrganizationIdAndTeamId(organization.getId(),
                            teamId)
                    .stream()
                    .map(PullRequestCurveMapper::dtoToView)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to read all PR time to merge curve for organization {}", organization, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to read all PR time to merge curve for organization")
                    .build();
        }
    }

    @Override
    public List<PullRequestView> readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(final UUID teamId,
                                                                                                        final Date startDate,
                                                                                                        final Date endDate,
                                                                                                        final int pageIndex,
                                                                                                        final int pageSize,
                                                                                                        final String sortingParameter,
                                                                                                        final String sortingDirection) throws SymeoException {
        try {
            final Pagination pagination = buildPagination(pageIndex, pageSize);
            return customPullRequestViewRepository.findAllByTeamIdAndStartEndAndEndDateAndPagination(
                    teamId, startDate, endDate, pagination.getStart(), pagination.getEnd(),
                    sortingParameterToDatabaseAttribute(sortingParameter),
                    directionToPostgresSortingValue(sortingDirection)
            ).stream().map(
                    PullRequestMapper::fullViewToDomain
            ).toList();
        } catch (Exception e) {
            final String message = String.format("Failed to find all PR details for teamId %s", teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }

    }

    @Override
    public int countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(final UUID teamId,
                                                                                 final Date startDate,
                                                                                 final Date endDate) throws SymeoException {
        try {
            return pullRequestFullViewRepository.countByTeamIdAndStartEndAndEndDate(teamId, startDate, endDate);
        } catch (Exception e) {
            final String message = String.format("Failed to count PR details for teamId %s", teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }


    @Override
    public List<PullRequestView> readPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(UUID teamId,
                                                                                            Date startDate,
                                                                                            Date endDate) throws SymeoException {
        return readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId, startDate, endDate, 0,
                200, "creation_date", "asc");
    }
}
