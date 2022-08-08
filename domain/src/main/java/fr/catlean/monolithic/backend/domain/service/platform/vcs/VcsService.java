package fr.catlean.monolithic.backend.domain.service.platform.vcs;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class VcsService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public void collectPullRequestsForOrganization(final Organization organization) throws CatleanException {
        getPullRequestsForOrganizationAccount(organization);
    }

    private void getPullRequestsForOrganizationAccount(final Organization organization) throws CatleanException {
        deliveryQuery.readRepositoriesForOrganization(organization)
                .parallelStream()
                .forEach(
                        repo -> {
                            try {
                                expositionStorageAdapter.savePullRequestDetails(collectPullRequestForRepository(repo)
                                        .stream()
                                        .map(pullRequest -> pullRequest.toBuilder()
                                                .organizationId(organization.getId())
                                                .vcsOrganizationId(organization.getVcsOrganization().getId())
                                                .build()
                                        )
                                        .toList());
                            } catch (CatleanException e) {
                                // TODO : add Logger to begin
                                LOGGER.error("Error while collecting PR for repo {}", repo);
                            }
                        }
                );
    }

    private List<PullRequest> collectPullRequestForRepository(final Repository repository) throws CatleanException {
        return deliveryCommand.collectPullRequestsForRepository(repository);
    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws CatleanException {
        return deliveryCommand.collectRepositoriesForOrganization(organization);
    }
}
