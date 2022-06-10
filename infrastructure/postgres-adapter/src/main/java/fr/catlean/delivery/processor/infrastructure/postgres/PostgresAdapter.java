package fr.catlean.delivery.processor.infrastructure.postgres;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.insight.PullRequestHistogram;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestEntity;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestHistogramDataEntity;
import fr.catlean.delivery.processor.infrastructure.postgres.mapper.PullRequestHistogramMapper;
import fr.catlean.delivery.processor.infrastructure.postgres.mapper.PullRequestMapper;
import fr.catlean.delivery.processor.infrastructure.postgres.repository.PullRequestHistogramRepository;
import fr.catlean.delivery.processor.infrastructure.postgres.repository.PullRequestRepository;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class PostgresAdapter implements ExpositionStorage {

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
}
