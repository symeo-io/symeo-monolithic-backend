package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
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
        List<String> allBranches = collectAllBranchesForOrganizationAndRepository(organization, repository);
        for (String branch : allBranches) {
            collectCommitsForForOrganizationAndRepositoryAndBranch(organization, repository, branch);
        }
        LOGGER.info("VCS data collection finished for organization {} and repository {}", organization, repository);
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


    public void collectCommitsForOrganization(Organization organization) throws SymeoException {
        for (Repository repository : deliveryQuery.readRepositoriesForOrganization(organization)) {
            List<Commit> commits = deliveryCommand.collectCommitsForRepository(repository);
            expositionStorageAdapter.saveCommits(commits);
        }
    }

    private List<String> collectAllBranchesForOrganizationAndRepository(final Organization organization,
                                                                        final Repository repository) {
        return List.of();
    }

    private void collectCommitsForForOrganizationAndRepositoryAndBranch(final Organization organization,
                                                                        final Repository repository,
                                                                        final String branch) {

    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        return deliveryCommand.collectRepositoriesForOrganization(organization);
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
