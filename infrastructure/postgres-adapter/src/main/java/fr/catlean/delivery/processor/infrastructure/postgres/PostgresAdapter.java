package fr.catlean.delivery.processor.infrastructure.postgres;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestEntity;
import fr.catlean.delivery.processor.infrastructure.postgres.mapper.PullRequestMapper;
import fr.catlean.delivery.processor.infrastructure.postgres.repository.PullRequestRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PostgresAdapter implements ExpositionStorage {

    private final PullRequestRepository pullRequestRepository;

    @Override
    public void savePullRequestDetails(List<PullRequest> pullRequests) {
        final List<PullRequestEntity> pullRequestEntities = pullRequests.stream().map(PullRequestMapper::domainToEntity)
                .toList();
        pullRequestRepository.saveAll(pullRequestEntities);
    }
}
