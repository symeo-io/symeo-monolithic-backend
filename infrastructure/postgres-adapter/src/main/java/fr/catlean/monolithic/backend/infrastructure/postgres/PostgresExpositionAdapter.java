package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestCurveMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestTimeToMergeRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
@Slf4j
public class PostgresExpositionAdapter implements ExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;

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
    public List<PullRequest> findAllPullRequestsForOrganizationAndTeamId(Organization organization, UUID teamId) throws CatleanException {
        try {
            return ofNullable(teamId)
                    .map(uuid -> pullRequestRepository.findAllByOrganizationIdAndTeamId(organization.getId(), uuid))
                    .orElseGet(() -> pullRequestRepository.findAllByOrganizationId(organization.getId()))
                    .stream().map(PullRequestMapper::entityToDomain).toList();
        } catch (Exception e) {
            LOGGER.error("Failed to find all pull requests for organization {}", organization, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find all pull requests for organization")
                    .build();
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<PullRequestTimeToMergeView> readPullRequestsTimeToMergeViewForOrganizationAndTeam(Organization organization,
                                                                                                  UUID teamId) throws CatleanException {
        try {
            return ofNullable(teamId)
                    .map(uuid -> pullRequestTimeToMergeRepository.findTimeToMergeDTOsByOrganizationIdAndTeamId(organization.getId(), uuid))
                    .orElseGet(() -> pullRequestTimeToMergeRepository.findTimeToMergeDTOsByOrganizationId(organization.getId()))
                    .stream()
                    .map(PullRequestCurveMapper::dtoToView)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to read all PR time to merge curve for organization {}", organization, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to read all PR time to merge curve for organization")
                    .build();
        }
    }
}
