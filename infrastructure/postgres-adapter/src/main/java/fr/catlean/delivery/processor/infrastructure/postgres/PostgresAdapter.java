package fr.catlean.delivery.processor.infrastructure.postgres;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;

import java.util.List;

public class PostgresAdapter implements ExpositionStorage {

    @Override
    public void savePullRequestDetails(List<PullRequest> pullRequests) {

    }
}
