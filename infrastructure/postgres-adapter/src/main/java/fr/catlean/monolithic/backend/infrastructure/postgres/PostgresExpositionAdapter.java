package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestHistogramDataEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestHistogramMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class PostgresExpositionAdapter implements ExpositionStorageAdapter {

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestHistogramRepository pullRequestHistogramRepository;

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
    public PullRequestHistogram readPullRequestHistogram(String organizationName, String teamName,
                                                         String histogramType) {
        final List<PullRequestHistogramDataEntity> histogramDataEntities =
                pullRequestHistogramRepository.findByOrganizationNameAndTeamNameAndHistogramType(organizationName,
                        teamName, histogramType);
        return PullRequestHistogramMapper.entitiesToDomain(histogramDataEntities);
    }
}