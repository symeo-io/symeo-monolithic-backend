package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Branch;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class VcsService {

    private final DeliveryCommand deliveryCommand;
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public void collectVcsDataForOrganizationAndRepositoryFromLastCollectionDate(final Organization organization,
                                                                                 Repository repository,
                                                                                 final Date lastCollectionDate) throws SymeoException {
        repository = populateRepositoryWithOrganizationId(repository, organization.getId());
        LOGGER.info("Starting to collect VCS data for organization {} and repository {}", organization, repository);
        collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(repository);
        final List<String> allBranches = collectAllBranchesForOrganizationAndRepository(organization, repository)
                .stream()
                .map(Branch::getName).toList();
        collectCommitsForForOrganizationAndRepositoryAndBranchesFromLastCollectionDate(organization, repository,
                allBranches, lastCollectionDate);
        LOGGER.info("VCS data collection finished for organization {} and repository {}", organization, repository);
    }

    public void collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        final List<Repository> repositories = deliveryCommand.collectRepositoriesForOrganization(organization).stream()
                .map(repository -> repository.toBuilder()
                        .organizationId(organization.getId())
                        .vcsOrganizationName(organization.getVcsOrganization().getName())
                        .vcsOrganizationId(organization.getVcsOrganization().getVcsId())
                        .build()).toList();
        expositionStorageAdapter.saveRepositories(repositories);
    }

    private void collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(Repository repository) throws SymeoException {
        expositionStorageAdapter.savePullRequestDetailsWithLinkedCommitsAndComments(
                collectPullRequestForRepository(repository).stream()
                        .map(pullRequest -> pullRequest.toBuilder()
                                .organizationId(repository.getOrganizationId())
                                .vcsOrganizationId(repository.getVcsOrganizationId())
                                .build())
                        .toList()
        );
    }

    private Repository populateRepositoryWithOrganizationId(final Repository repository,
                                                            final UUID organizationId) {
        return repository.toBuilder().organizationId(organizationId).build();
    }

    private List<Branch> collectAllBranchesForOrganizationAndRepository(final Organization organization,
                                                                        final Repository repository) throws SymeoException {
        return deliveryCommand.collectBranchesForOrganizationAndRepository(organization, repository);
    }

    private void collectCommitsForForOrganizationAndRepositoryAndBranchesFromLastCollectionDate(final Organization organization,
                                                                                                final Repository repository,
                                                                                                final List<String> branches,
                                                                                                final Date lastCollectionDate) throws SymeoException {
        expositionStorageAdapter.saveCommits(
                deliveryCommand.collectCommitsForForOrganizationAndRepositoryAndBranchesFromLastCollectionDate(organization,
                                repository, branches, lastCollectionDate)
                        .stream()
                        .map(commit -> commit.toBuilder().repositoryId(repository.getId()).build())
                        .toList()
        );
    }

    private List<PullRequest> collectPullRequestForRepository(final Repository repository) throws SymeoException {
        return deliveryCommand.collectPullRequestsForRepository(repository);
    }

}
