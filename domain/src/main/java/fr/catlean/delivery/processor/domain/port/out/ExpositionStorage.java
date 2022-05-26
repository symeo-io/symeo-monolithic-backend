package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.PullRequest;

import java.util.List;

public interface ExpositionStorage {
    void savePullRequestDetails(List<PullRequest> pullRequests);
}
