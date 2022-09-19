package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.query.DeliveryQuery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class VcsService {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public void collectVcsDataForOrganizationAndRepository(final Organization organization,
                                                           Repository repository) throws SymeoException {
        repository = populateRepositoryWithOrganizationId(repository, organization.getId());
        LOGGER.info("Starting to collect VCS data for organization {} and repository {}", organization, repository);
        collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repository);
        final List<String> allBranches = collectAllBranchesForOrganizationAndRepository(organization, repository)
                .stream()
                .map(Branch::getName).toList();
        for (String branch : allBranches) {
            collectCommitsForForOrganizationAndRepositoryAndBranch(organization, repository, branch);
        }
        LOGGER.info("VCS data collection finished for organization {} and repository {}", organization, repository);
    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        return deliveryCommand.collectRepositoriesForOrganization(organization);
    }

    private void collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(Organization organization,
                                                                                       Repository repository) throws SymeoException {
        final List<PullRequest> pullRequests = new ArrayList<>();
        for (PullRequest pullRequest : collectPullRequestForRepository(repository)) {
            final PullRequest updatedPullRequest = pullRequest.toBuilder()
                    .organizationId(organization.getId())
                    .vcsOrganizationId(organization.getVcsOrganization().getVcsId())
                    .build();
            pullRequests.add(updatedPullRequest.toBuilder()
                    .commits(collectCommitsForRepositoryAndPullRequest(repository, updatedPullRequest))
                    .comments(collectCommentsForRepositoryAndPullRequest(repository, updatedPullRequest))
                    .build());
        }
        expositionStorageAdapter.savePullRequestDetailsWithLinkedCommitsAndComments(pullRequests);
    }

    private Repository populateRepositoryWithOrganizationId(final Repository repository,
                                                            final UUID organizationId) {
        return repository.toBuilder().organizationId(organizationId).build();
    }

    private List<Branch> collectAllBranchesForOrganizationAndRepository(final Organization organization,
                                                                        final Repository repository) throws SymeoException {
        return deliveryCommand.collectBranchesForOrganizationAndRepository(organization, repository);
    }

    private void collectCommitsForForOrganizationAndRepositoryAndBranch(final Organization organization,
                                                                        final Repository repository,
                                                                        final String branch) {

    }

    private List<PullRequest> collectPullRequestForRepository(final Repository repository) throws SymeoException {
        return deliveryCommand.collectPullRequestsForRepository(repository);
    }

    private List<Commit> collectCommitsForRepositoryAndPullRequest(final Repository repository,
                                                                   final PullRequest pullRequest) throws SymeoException {
        return deliveryCommand.collectCommitsForPullRequest(repository, pullRequest);
    }

    private List<Comment> collectCommentsForRepositoryAndPullRequest(final Repository repository,
                                                                     final PullRequest pullRequest)
            throws SymeoException {
        return deliveryCommand.collectCommentsForRepositoryAndPullRequest(repository, pullRequest);
    }
}
