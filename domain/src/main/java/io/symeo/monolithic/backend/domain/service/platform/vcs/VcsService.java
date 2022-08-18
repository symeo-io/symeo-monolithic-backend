package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class VcsService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public void collectPullRequestsForOrganization(final Organization organization) throws SymeoException {
        getPullRequestsForOrganizationAccount(organization);
    }

    private void getPullRequestsForOrganizationAccount(final Organization organization) throws SymeoException {
        final AtomicReference<SymeoException> symeoExceptionAtomicReference = new AtomicReference<>();
        deliveryQuery.readRepositoriesForOrganization(organization)
                .stream()
                .map(repository -> repository.toBuilder().organizationId(organization.getId()).build())
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
                            } catch (SymeoException e) {
                                LOGGER.error("Error while collecting PR for repo {}", repo, e);
                                symeoExceptionAtomicReference.set(e);
                            }
                        }
                );
        if (nonNull(symeoExceptionAtomicReference.get())) {
            throw symeoExceptionAtomicReference.get();
        }
    }

    private List<PullRequest> collectPullRequestForRepository(final Repository repository) throws SymeoException {
        return deliveryCommand.collectPullRequestsForRepository(repository);
    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        return deliveryCommand.collectRepositoriesForOrganization(organization);
    }
}
