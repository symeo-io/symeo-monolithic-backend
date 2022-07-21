package fr.catlean.monolithic.backend.domain.service.platform.vcs;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Slf4j
public class VcsService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;

    public List<PullRequest> collectPullRequestsForOrganization(final Organization organization) throws CatleanException {
        return getPullRequestsForOrganizationAccount(organization).stream()
                .map(pullRequest -> populateWithOrganizationAccount(pullRequest, organization)).toList();
    }

    private PullRequest populateWithOrganizationAccount(final PullRequest pullRequest,
                                                        final Organization organization) {
        return organization.getTeams().stream()
                .filter(team -> team.getRepositories().stream().map(Repository::getName).toList().contains(pullRequest.getRepository()))
                .findFirst()
                .map(vcsTeam -> pullRequest.toBuilder()
                        .team(vcsTeam.getName())
                        .vcsOrganization(organization.getName())
                        .organization(organization.getName())
                        .build()
                ).orElse(pullRequest);
    }

    private List<PullRequest> getPullRequestsForOrganizationAccount(final Organization organization) throws CatleanException {
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganization(organization);
        return repositories.parallelStream()
                .filter(repository -> filterRepositoryForOrganizationAccount(organization, repository))
                .map(
                        repo -> {
                            try {
                                return collectPullRequestForRepository(repo);
                            } catch (CatleanException e) {
                                // TODO : add Logger to begin
                                LOGGER.error("Error while collecting PR for repo {}", repo);
                            }
                            return new ArrayList<PullRequest>();
                        }
                ).flatMap(Collection::stream).toList();
    }

    private static boolean filterRepositoryForOrganizationAccount(final Organization organization,
                                                                  final Repository repository) {
        final List<String> allTeamsRepositories =
                organization.getAllTeamsRepositories();
        return allTeamsRepositories.isEmpty() || allTeamsRepositories.contains(repository.getName());
    }

    private List<PullRequest> collectPullRequestForRepository(final Repository repository) throws CatleanException {
        deliveryCommand.collectPullRequestsForRepository(repository);
        final List<PullRequest> pullRequestList = deliveryQuery.readPullRequestsForRepository(repository);
        return isNull(pullRequestList) ? List.of() : pullRequestList;
    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws CatleanException {
        deliveryCommand.collectRepositoriesForOrganization(organization);
        return deliveryQuery.readRepositoriesForOrganization(organization);
    }
}
