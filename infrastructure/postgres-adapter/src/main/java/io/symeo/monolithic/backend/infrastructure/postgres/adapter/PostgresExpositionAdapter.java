package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.pagination.Pagination;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.buildPagination;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.SortingMapper.directionToPostgresSortingValue;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.CycleTimeMapper.*;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper.pullRequestSortingParameterToDatabaseAttribute;

@AllArgsConstructor
@Slf4j
public class PostgresExpositionAdapter implements DataProcessingExpositionStorageAdapter, BffExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;
    private final PullRequestSizeRepository pullRequestSizeRepository;
    private final PullRequestFullViewRepository pullRequestFullViewRepository;
    private final CustomPullRequestViewRepository customPullRequestViewRepository;
    private final CustomPullRequestWithCommitsAndCommentsRepository customPullRequestWithCommitsAndCommentsRepository;
    private final CommitRepository commitRepository;
    private final TagRepository tagRepository;
    private final CustomCommitRepository customCommitRepository;
    private final VcsOrganizationRepository vcsOrganizationRepository;
    private final CustomCycleTimeRepository customCycleTimeRepository;
    private final CycleTimeRepository cycleTimeRepository;

    @Override
    public List<PullRequest> savePullRequestDetailsWithLinkedComments(List<PullRequest> pullRequests) {
        pullRequestRepository.saveAll(pullRequests.stream().map(PullRequestMapper::domainToEntity)
                .toList());
        return pullRequests;
    }

    @Override
    public void saveRepositories(List<Repository> repositories) {
        repositoryRepository.saveAll(repositories.stream().map(RepositoryMapper::domainToEntity).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepositoryView> readRepositoriesForOrganization(Organization organization) {
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
                    pullRequestSortingParameterToDatabaseAttribute(sortingParameter),
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
    public List<PullRequestView> findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(UUID teamId,
                                                                                              Date startDate,
                                                                                              Date endDate,
                                                                                              int pageIndex,
                                                                                              int pageSize,
                                                                                              String sortingParameter,
                                                                                              String sortingDirection) throws SymeoException {
        try {
            final Pagination pagination = buildPagination(pageIndex, pageSize);
            return customPullRequestViewRepository.findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(
                    teamId, startDate, endDate, pagination.getStart(), pagination.getEnd(),
                    pullRequestSortingParameterToDatabaseAttribute(sortingParameter),
                    directionToPostgresSortingValue(sortingDirection));
        } catch (Exception e) {
            final String message = String.format("Failed to find all PR details paginated and sorted with %s for " +
                    "teamId %s until endDate %s", sortingParameter, teamId, endDate);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }

    }

    @Override
    public List<CycleTimePiece> findCycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted(UUID teamId, Date startDate, Date endDate, Integer pageIndex, Integer pageSize, String sortingParameter, String sortingDirection) throws SymeoException {
        try {
            final Pagination pagination = buildPagination(pageIndex, pageSize);
            return customCycleTimeRepository.findAllCycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted(
                    teamId, startDate, endDate, pagination.getStart(), pagination.getEnd(),
                    cycleTimePieceSortingParameterToDataBaseAttribute(sortingParameter),
                    directionToPostgresSortingValue(sortingDirection)
            );
        } catch (Exception e) {
            final String message = String.format("Failed to read cycle times for teamId %s between startDate %s and endDate %s", teamId, startDate, endDate);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<CycleTimePiece> findCycleTimePiecesForTeamIdBetweenStartDateAndEndDate(UUID teamId, Date startDate, Date endDate) throws SymeoException {
        try {
            return customCycleTimeRepository.findAllCycleTimePiecesForTeamIdBetweenStartDateAndEndDate(
                    teamId, startDate, endDate
            );
        } catch (Exception e) {
            final String message = String.format("Failed to read cycle times for teamId %s between startDate %s and endDate %s", teamId, startDate, endDate);
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
    public List<PullRequestView> readPullRequestsWithCommitsForTeamIdUntilEndDate(UUID teamId,
                                                                                  Date endDate) throws SymeoException {
        try {
            return customPullRequestWithCommitsAndCommentsRepository.findAllByTeamIdUntilEndDate(teamId,
                    endDate);
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
    public List<CycleTime> findCycleTimesForTeamIdBetweenStartDateAndEndDate(UUID teamId, Date startDate, Date endDate) throws SymeoException {
        try {
            return customCycleTimeRepository.findAllCycleTimeByTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate);

        } catch (Exception e) {
            final String message = String.format("Failed to read cycle times for teamId %s between startDate %s and endDate %s", teamId, startDate, endDate);
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
    public List<RepositoryView> findAllRepositoriesForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
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
                    .map(RepositoryMapper::entityToDataProcessingDomain)
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
    public void saveCycleTimes(List<io.symeo.monolithic.backend.job.domain.model.vcs.CycleTime> cycleTimes) throws SymeoException {
        try {
            LOGGER.info("Saving {} cycle times to database", cycleTimes.size());
            cycleTimeRepository.saveAll(cycleTimes.stream().map(CycleTimeMapper::domainToEntity).toList());
        } catch (Exception e) {
            final String message = "Failed to save cycle times";
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
    public List<Commit> readAllCommitsForRepositoryId(String repositoryId) throws SymeoException {
        try {
            return commitRepository.findAllForRepositoryId(repositoryId)
                    .stream()
                    .map(CommitMapper::entityToDomain)
                    .collect(Collectors.toSet())
                    .stream().toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read commits for repositoryId %s", repositoryId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<PullRequest> readMergedPullRequestsForRepositoryIdUntilEndDate(String repositoryId, Date endDate) throws SymeoException {
        try {
            return pullRequestRepository.findAllMergedPullRequestsForRepositoryIdUntilEndDate(repositoryId, endDate)
                    .stream()
                    .map(PullRequestMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read PR for repositoryId %s until endDate %s", repositoryId, endDate);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<Tag> readTagsForRepositoryId(String repositoryId) throws SymeoException {
        try {
            return tagRepository.findAllForRepositoryId(repositoryId).stream()
                    .map(TagMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read tags for repositoryId %s", repositoryId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }    }

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
    public List<PullRequestView> readMergedPullRequestsForTeamIdUntilEndDate(UUID teamId, Date endDate) throws SymeoException {
        try {
            return pullRequestFullViewRepository.findAllMergedPullRequestsForTeamIdUntilEndDate(teamId, endDate)
                    .stream()
                    .map(PullRequestMapper::fullViewToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read PR for teamId %s until endDate %s", teamId, endDate);
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
    public List<CommitView> readAllCommitsForTeamId(UUID teamId) throws SymeoException {
        try {
            return customCommitRepository.findAllByTeamId(teamId);
        } catch (Exception e) {
            final String message = String.format("Failed to read commits for teamId %s", teamId);
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
    public List<TagView> findTagsForTeamId(UUID teamId) throws SymeoException {
        try {
            return tagRepository.findAllForTeamId(teamId).stream()
                    .map(TagMapper::entityToDomainView)
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

    @Override
    public List<CommitView> readCommitsMatchingShaListBetweenStartDateAndEndDate(List<String> shaList, Date startDate,
                                                                                 Date endDate) throws SymeoException {
        try {
            return commitRepository.findAllForShaListBetweenStartDateAndEndDate(shaList, startDate, endDate).stream()
                    .map(CommitMapper::entityToDomainView)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read commits for shaList %s between startDate %s and " +
                    "endDate %s", shaList, startDate, endDate);
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
    public Optional<VcsOrganization> findVcsOrganizationByIdAndOrganizationId(Long vcsOrganizationId,
                                                                              UUID organizationId) throws SymeoException {
        try {
            return vcsOrganizationRepository.findByIdAndOrganizationId(vcsOrganizationId, organizationId)
                    .map(OrganizationMapper::dataProcessingVcsEntityToDomain);
        } catch (Exception e) {
            final String message = String.format("Failed to find vcsOrganization of organizationId %s and " +
                    "vcsOrganizationId %s", organizationId, vcsOrganizationId);
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
    public List<Repository> findAllRepositoriesByIds(List<String> repositoryIds) throws SymeoException {
        try {
            return repositoryRepository.findAllByIdIn(repositoryIds).stream()
                    .map(RepositoryMapper::entityToDataProcessingDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read repositories for ids %s", repositoryIds);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
