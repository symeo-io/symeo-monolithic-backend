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
                        repository -> {
                            try {
                                collectAndSavePullRequestsDetailsWithCommitsAndComments(organization,
                                        symeoExceptionAtomicReference, repository);
                            } catch (SymeoException e) {
                                LOGGER.error("Error while collecting PR for repository {}", repository, e);
                                symeoExceptionAtomicReference.set(e);
                            }
                        }
                );
        if (nonNull(symeoExceptionAtomicReference.get())) {
            throw symeoExceptionAtomicReference.get();
        }
    }

    private void collectAndSavePullRequestsDetailsWithCommitsAndComments(Organization organization,
                                                                         AtomicReference<SymeoException> symeoExceptionAtomicReference,
                                                                         Repository repository) throws SymeoException {
        expositionStorageAdapter.savePullRequestDetailsWithLinkedCommitsAndComments(collectPullRequestForRepository(repository)
                .stream()
                .map(pullRequest -> pullRequest.toBuilder()
                        .organizationId(organization.getId())
                        .vcsOrganizationId(organization.getVcsOrganization().getId())
                        .build()
                )
                .map(pullRequest -> populatePullRequestWithCommits(symeoExceptionAtomicReference, repository,
                        pullRequest))
                .map(pullRequest -> populatePullRequestWithComments(symeoExceptionAtomicReference, repository,
                        pullRequest))
                .toList());
    }

    private PullRequest populatePullRequestWithCommits(final AtomicReference<SymeoException> symeoExceptionAtomicReference,
                                                       final Repository repository, final PullRequest pullRequest) {
        List<Commit> commits;
        try {
            commits = collectCommitsForRepositoryAndPullRequest(repository, pullRequest);
        } catch (SymeoException e) {
            LOGGER.error("Error while collection commits for PR {}",
                    pullRequest, e);
            symeoExceptionAtomicReference.set(e);
            commits = new ArrayList<>();
        }
        return pullRequest.toBuilder()
                .commits(commits)
                .build();
    }

    private PullRequest populatePullRequestWithComments(final AtomicReference<SymeoException> symeoExceptionAtomicReference,
                                                        final Repository repository, final PullRequest pullRequest) {
        List<Comment> comments;
        try {
            comments = collectCommentsForRepositoryAndPullRequest(repository, pullRequest);
        } catch (SymeoException e) {
            LOGGER.error("Error while collection comments for PR {}",
                    pullRequest, e);
            symeoExceptionAtomicReference.set(e);
            comments = new ArrayList<>();
        }
        return pullRequest.toBuilder()
                .comments(comments)
                .build();
    }


    private List<PullRequest> collectPullRequestForRepository(final Repository repository) throws SymeoException {
        return deliveryCommand.collectPullRequestsForRepository(repository);
    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        return deliveryCommand.collectRepositoriesForOrganization(organization);
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
