package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.pagination.Pagination;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.buildPagination;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.SortingMapper.directionToPostgresSortingValue;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper.sortingParameterToDatabaseAttribute;

@AllArgsConstructor
@Slf4j
public class PostgresExpositionAdapter implements ExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;
    private final PullRequestSizeRepository pullRequestSizeRepository;
    private final PullRequestFullViewRepository pullRequestFullViewRepository;
    private final CustomPullRequestViewRepository customPullRequestViewRepository;
    private final PullRequestWithCommitsAndCommentsRepository pullRequestWithCommitsAndCommentsRepository;
    private final CommitRepository commitRepository;
    private final TagRepository tagRepository;

    @Override
    public void savePullRequestDetailsWithLinkedCommitsAndComments(List<PullRequest> pullRequests) {
        pullRequestRepository.saveAll(pullRequests.stream().map(PullRequestMapper::domainToEntity)
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
    public List<PullRequestView> readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(final Organization organization,
                                                                                                                 final UUID teamId,
                                                                                                                 final Date startDate,
                                                                                                                 final Date endDate) throws SymeoException {
        try {
            return pullRequestTimeToMergeRepository.findTimeToMergeDTOsByOrganizationIdAndTeamIdBetweenStartDateAndEndDate(
                            organization.getId(), teamId, startDate, endDate)
                    .stream()
                    .map(PullRequestCurveMapper::dtoToView)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to read all PR time to merge curve for organization {}", organization, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to read all PR time to merge curve for organization")
                    .build();
        }
    }

    @Override
    public List<PullRequestView> readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(Organization organization,
                                                                                                         UUID teamId,
                                                                                                         Date startDate,
                                                                                                         Date endDate) throws SymeoException {
        try {
            return pullRequestSizeRepository.findPullRequestSizeDTOsByOrganizationIdAndTeamIdForBetweenStartDateAndEndDate(organization.getId(),
                            teamId, startDate, endDate)
                    .stream()
                    .map(PullRequestCurveMapper::dtoToView)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to read all PR time to merge curve for organization {}", organization, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .rootException(e)
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
                    .rootException(e)
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
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<PullRequestView> readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(UUID teamId,
                                                                                        Date endDate) throws SymeoException {
        try {
            return pullRequestWithCommitsAndCommentsRepository.findAllByTeamIdUntilEndDate(teamId,
                            endDate)
                    .stream()
                    .map(PullRequestMapper::withCommitsAndCommentsToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read PR with commits and comments for teamId %s", teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String findDefaultMostUsedBranchForOrganizationId(UUID organizationId) throws SymeoException {
        try {
            return repositoryRepository.findDefaultMostUsedBranchForOrganizationId(organizationId);
        } catch (Exception e) {
            final String message = String.format("Failed to find default most used branch for organizationId %s",
                    organizationId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void saveCommits(List<Commit> commits) throws SymeoException {
        try {
            LOGGER.info("Saving {} commit(s) to database", commits.size());
            commitRepository.saveAll(commits.stream().map(CommitMapper::domainToEntity).toList());
        } catch (Exception e) {
            final String message = "Failed to save commits";
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .rootException(e)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Repository> findAllRepositoriesForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
        try {
            return repositoryRepository.findAllRepositoriesForOrganizationIdAndTeamId(organizationId, teamId)
                    .stream()
                    .map(RepositoryMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to find repositories for organizationId %s and teamId %s",
                    organizationId, teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Repository> findAllRepositoriesLinkedToTeamsForOrganizationId(UUID organizationId) throws SymeoException {
        try {
            return repositoryRepository.findAllRepositoriesLinkedToTeamsForOrganizationId(organizationId)
                    .stream()
                    .map(RepositoryMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to find repositories for organizationId %s",
                    organizationId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestView> readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(final UUID teamId,
                                                                                           final Date startDate,
                                                                                           final Date endDate) throws SymeoException {
        try {
            return pullRequestFullViewRepository.findAllMergedPullRequestsForTeamIdBetweenStartDateAndDate(teamId,
                            startDate, endDate)
                    .stream()
                    .map(PullRequestMapper::fullViewToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read PR for teamId %s from startDate %s", teamId,
                    startDate);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Commit> readAllCommitsForTeamIdUntilEndDate(UUID teamId, Date endDate) throws SymeoException {
        try {
            return commitRepository.findAllByTeamIdUntilEndDate(teamId, endDate)
                    .stream()
                    .map(CommitMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read commits for teamId %s until endDate %s", teamId,
                    endDate);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void saveTags(List<Tag> tags) throws SymeoException {
        try {
            tagRepository.saveAll(tags.stream()
                    .map(TagMapper::domainToEntity)
                    .toList());
        } catch (Exception e) {
            final String message = String.format("Failed to save %s tag(s)", tags.size());
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<Tag> findTagsForTeamId(UUID teamId) throws SymeoException {
        try {
            return tagRepository.findAllForTeamId(teamId).stream()
                    .map(TagMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read tags for teamId %s", teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
