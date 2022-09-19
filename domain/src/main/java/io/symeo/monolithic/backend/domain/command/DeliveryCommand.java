package io.symeo.monolithic.backend.domain.command;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.util.List;

public class DeliveryCommand {

    private final RawStorageAdapter rawStorageAdapter;
    private final VersionControlSystemAdapter versionControlSystemAdapter;

    public DeliveryCommand(
            RawStorageAdapter rawStorageAdapter,
            VersionControlSystemAdapter versionControlSystemAdapter) {
        this.rawStorageAdapter = rawStorageAdapter;
        this.versionControlSystemAdapter = versionControlSystemAdapter;
    }

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        final byte[] rawRepositories =
                versionControlSystemAdapter.getRawRepositories(organization.getVcsOrganization().getName());
        rawStorageAdapter.save(
                organization.getId(),
                versionControlSystemAdapter.getName(),
                Repository.ALL,
                rawRepositories);
        return versionControlSystemAdapter.repositoriesBytesToDomain(rawRepositories);
    }

    public List<PullRequest> collectPullRequestsForRepository(Repository repository) throws SymeoException {
        byte[] alreadyRawPullRequestsCollected = null;
        if (rawStorageAdapter.exists(repository.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepositoryId(repository.getId()))) {
            alreadyRawPullRequestsCollected = rawStorageAdapter.read(repository.getOrganizationId(),
                    versionControlSystemAdapter.getName(), PullRequest.getNameFromRepositoryId(repository.getId()));
        }
        final byte[] rawPullRequestsForRepository =
                versionControlSystemAdapter.getRawPullRequestsForRepository(repository,
                        alreadyRawPullRequestsCollected);
        rawStorageAdapter.save(
                repository.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepositoryId(repository.getId()),
                rawPullRequestsForRepository);
        return versionControlSystemAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
    }

    public List<Commit> collectCommitsForPullRequest(final Repository repository, final PullRequest pullRequest) throws SymeoException {
        final byte[] rawCommits =
                versionControlSystemAdapter.getRawCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), pullRequest.getNumber());
        rawStorageAdapter.save(
                pullRequest.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                Commit.getNameFromPullRequest(pullRequest),
                rawCommits);
        return versionControlSystemAdapter.commitsBytesToDomain(rawCommits);
    }

    public List<Comment> collectCommentsForRepositoryAndPullRequest(final Repository repository,
                                                                    final PullRequest pullRequest) throws SymeoException {
        final byte[] rawComments = versionControlSystemAdapter.getRawComments(repository.getVcsOrganizationName(),
                repository.getName(), pullRequest.getNumber());
        rawStorageAdapter.save(
                pullRequest.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                Comment.getNameFromPullRequest(pullRequest),
                rawComments);
        return versionControlSystemAdapter.commentsBytesToDomain(rawComments);
    }

    public List<Commit> collectCommitsForRepository(Repository repository) throws SymeoException {
        byte[] alreadyCollectedCommits = null;
        if (rawStorageAdapter.exists(repository.getOrganizationId(), versionControlSystemAdapter.getName(),
                Commit.getNameFromRepository(repository))) {
            alreadyCollectedCommits = rawStorageAdapter.read(repository.getOrganizationId(),
                    versionControlSystemAdapter.getName(), Commit.getNameFromRepository(repository));
        }
        final byte[] rawCommitsForRepository =
                versionControlSystemAdapter.getRawCommitsForRepository(repository.getVcsOrganizationName(),
                        repository.getName(), alreadyCollectedCommits);
        rawStorageAdapter.save(
                repository.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                Commit.getNameFromRepository(repository),
                rawCommitsForRepository
        );
        return versionControlSystemAdapter.commitsBytesToDomain(rawCommitsForRepository);
    }

    public List<Branch> collectBranchesForOrganizationAndRepository(final Organization organization,
                                                                    final Repository repository) throws SymeoException {
        final byte[] rawBranches =
                versionControlSystemAdapter.getRawBranches(organization.getVcsOrganization().getName(),
                        repository.getName());
        rawStorageAdapter.save(organization.getId(), versionControlSystemAdapter.getName(), Branch.ALL, rawBranches);
        return versionControlSystemAdapter.branchesBytesToDomain(rawBranches);
    }
}
