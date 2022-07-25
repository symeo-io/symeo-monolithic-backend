package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestHistogramDataEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestHistogramMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;

@AllArgsConstructor
@Slf4j
public class PostgresExpositionAdapter implements ExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestHistogramRepository pullRequestHistogramRepository;
    private final RepositoryRepository repositoryRepository;

    @Override
    public void savePullRequestDetails(List<PullRequest> pullRequests) {
        final List<PullRequestEntity> pullRequestEntities = pullRequests.stream().map(PullRequestMapper::domainToEntity)
                .toList();
        pullRequestRepository.saveAll(pullRequestEntities);
    }

    @Override
    public void savePullRequestHistograms(List<PullRequestHistogram> pullRequestHistograms) {
        final List<PullRequestHistogramDataEntity> pullRequestHistogramDataEntities =
                pullRequestHistograms.stream().map(PullRequestHistogramMapper::domainToEntities).flatMap(Collection::stream).toList();
        pullRequestHistogramRepository.saveAll(pullRequestHistogramDataEntities);
    }

    @Override
    public PullRequestHistogram readPullRequestHistogram(String organizationId, String teamName,
                                                         String histogramType) {
        final List<PullRequestHistogramDataEntity> histogramDataEntities =
                pullRequestHistogramRepository.findByOrganizationIdAndTeamNameAndHistogramType(organizationId,
                        teamName, histogramType);
        return PullRequestHistogramMapper.entitiesToDomain(histogramDataEntities);
    }

    @Override
    public void saveRepositories(List<Repository> repositories) {
        repositoryRepository.saveAll(repositories.stream().map(RepositoryMapper::domainToEntity).toList());
    }

    @Override
    public List<Repository> readRepositoriesForOrganization(Organization organization) {
        return repositoryRepository.findRepositoryEntitiesByOrganizationId(organization.getId().toString())
                .stream()
                .map(RepositoryMapper::entityToDomain)
                .toList();
    }

    @Override
    public List<PullRequest> findAllPullRequestsForOrganization(Organization organization) throws CatleanException {
        try {
            return pullRequestRepository.findAllByOrganizationId(organization.getId().toString())
                    .stream().map(PullRequestMapper::entityToDomain).toList();
        } catch (Exception e) {
            LOGGER.error("Failed to find all pull requests for organization {}", organization, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find all pull requests for organization")
                    .build();
        }
    }
}
