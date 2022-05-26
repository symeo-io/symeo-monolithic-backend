package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class DeliveryProcessorService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;

    public List<PullRequest> collectPullRequestsForOrganisation(String organisation) {
        deliveryCommand.collectRepositoriesForOrganisation(organisation);
        return deliveryQuery.readRepositoriesForOrganisation(organisation).stream().map(
                this::collectPullRequestForRepository
        ).flatMap(Collection::stream).toList();
    }

    private List<PullRequest> collectPullRequestForRepository(Repository repository) {
        deliveryCommand.collectPullRequestsForRepository(repository);
        final List<PullRequest> pullRequestList = deliveryQuery.readPullRequestsForRepository(repository);
        return isNull(pullRequestList) ? List.of() : pullRequestList;
    }
}