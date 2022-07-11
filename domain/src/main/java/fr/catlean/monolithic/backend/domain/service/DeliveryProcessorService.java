package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class DeliveryProcessorService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public List<PullRequest> collectPullRequestsForOrganization(final OrganizationAccount organizationAccount) {
        final List<PullRequest> pullRequests = getPullRequestsForOrganizationAccount(organizationAccount).stream()
                .map(pullRequest -> populateWithOrganizationAccount(pullRequest, organizationAccount)).toList();
        expositionStorageAdapter.savePullRequestDetails(pullRequests);
        return pullRequests;
    }

    private PullRequest populateWithOrganizationAccount(final PullRequest pullRequest,
                                                        final OrganizationAccount organizationAccount) {
        return organizationAccount.getVcsConfiguration().getVcsTeams().stream()
                .filter(vcsTeam -> vcsTeam.getVcsRepositoryNames().contains(pullRequest.getRepository()))
                .findFirst()
                .map(vcsTeam -> pullRequest.toBuilder()
                        .team(vcsTeam.getName())
                        .vcsOrganization(organizationAccount.getName())
                        .organization(organizationAccount.getName())
                        .build()
                ).orElse(pullRequest);
    }

    private List<PullRequest> getPullRequestsForOrganizationAccount(final OrganizationAccount organizationAccount) {
        deliveryCommand.collectRepositoriesForOrganization(organizationAccount);
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganization(organizationAccount);
        return repositories.parallelStream()
                .filter(repository -> filterRepositoryForOrganizationAccount(organizationAccount, repository))
                .map(
                        this::collectPullRequestForRepository
                ).flatMap(Collection::stream).toList();
    }

    private static boolean filterRepositoryForOrganizationAccount(final OrganizationAccount organizationAccount,
                                                                  final Repository repository) {
        final List<String> allTeamsRepositories =
                organizationAccount.getVcsConfiguration().getAllTeamsRepositories();
        return allTeamsRepositories.isEmpty() || allTeamsRepositories.contains(repository.getName());
    }

    private List<PullRequest> collectPullRequestForRepository(final Repository repository) {
        deliveryCommand.collectPullRequestsForRepository(repository);
        final List<PullRequest> pullRequestList = deliveryQuery.readPullRequestsForRepository(repository);
        return isNull(pullRequestList) ? List.of() : pullRequestList;
    }
}
