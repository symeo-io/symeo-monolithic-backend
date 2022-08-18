package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestCurveMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestSizeRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestTimeToMergeRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Slf4j
public class PostgresExpositionAdapter implements ExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;
    private final PullRequestSizeRepository pullRequestSizeRepository;

    @Override
    public void savePullRequestDetails(List<PullRequest> pullRequests) {
        final List<PullRequestEntity> pullRequestEntities = pullRequests.stream().map(PullRequestMapper::domainToEntity)
                .toList();
        pullRequestRepository.saveAll(pullRequestEntities);
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
}