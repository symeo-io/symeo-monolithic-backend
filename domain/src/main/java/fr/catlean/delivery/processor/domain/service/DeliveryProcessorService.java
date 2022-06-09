package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
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

    public List<PullRequest> collectPullRequestsForOrganisation(OrganisationAccount organisationAccount) {
        deliveryCommand.collectRepositoriesForOrganisation(organisationAccount);
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganisation(organisationAccount);
        final List<PullRequest> pullRequests =
                repositories.parallelStream()
                        .filter(repository -> filterRepositoryForOrganisationAccount(organisationAccount, repository))
                        .map(
                                this::collectPullRequestForRepository
                        ).flatMap(Collection::stream).toList();
        expositionStorage.savePullRequestDetails(pullRequests);
        return pullRequests;
    }

    private static boolean filterRepositoryForOrganisationAccount(OrganisationAccount organisationAccount,
                                                                  Repository repository) {
        final List<String> allTeamsRepositories =
                organisationAccount.getVcsConfiguration().getAllTeamsRepositories();
        return allTeamsRepositories.isEmpty() || allTeamsRepositories.contains(repository.getName());
    }

    private List<PullRequest> collectPullRequestForRepository(Repository repository) {
        deliveryCommand.collectPullRequestsForRepository(repository);
        final List<PullRequest> pullRequestList = deliveryQuery.readPullRequestsForRepository(repository);
        return isNull(pullRequestList) ? List.of() : pullRequestList;
    }
}
