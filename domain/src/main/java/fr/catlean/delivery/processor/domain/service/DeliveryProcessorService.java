package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import fr.catlean.delivery.processor.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class DeliveryProcessorService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;
    private final ExpositionStorage expositionStorage;

    public List<PullRequest> collectPullRequestsForOrganisation(String organisation) {
        deliveryCommand.collectRepositoriesForOrganisation(organisation);
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganisation(organisation);
//        repositories = repositories.subList(0,5);
        final List<PullRequest> pullRequests =
                repositories.stream()
                        .filter(repository -> !repository.getName().equals("saas-frontend"))
                        .map(
                                this::collectPullRequestForRepository
                        ).flatMap(Collection::stream).toList();
        expositionStorage.savePullRequestDetails(pullRequests);
        return pullRequests;

    }

    private List<PullRequest> collectPullRequestForRepository(Repository repository) {
        deliveryCommand.collectPullRequestsForRepository(repository);
        final List<PullRequest> pullRequestList = deliveryQuery.readPullRequestsForRepository(repository);
        return isNull(pullRequestList) ? List.of() : pullRequestList;
    }
}
