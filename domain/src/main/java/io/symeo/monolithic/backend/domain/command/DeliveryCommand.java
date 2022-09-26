package io.symeo.monolithic.backend.domain.command;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<Branch> collectBranchesForOrganizationAndRepository(final Organization organization,
                                                                    final Repository repository) throws SymeoException {
        final byte[] rawBranches =
                versionControlSystemAdapter.getRawBranches(organization.getVcsOrganization().getName(),
                        repository.getName());
        rawStorageAdapter.save(organization.getId(), versionControlSystemAdapter.getName(), Branch.ALL, rawBranches);
        return versionControlSystemAdapter.branchesBytesToDomain(rawBranches);
    }

    public List<Commit> collectCommitsForForOrganizationAndRepositoryAndBranchesFromLastCollectionDate(final Organization organization,
                                                                                                       final Repository repository,
                                                                                                       final List<String> branches,
                                                                                                       final Date lastCollectionDate) throws SymeoException {
        final Set<Commit> commitsCollected = new HashSet<>();
        for (String branchName : branches) {
            byte[] alreadyCollectedCommits = null;
            final String contentName = Commit.getNameFromBranch(branchName);
            if (rawStorageAdapter.exists(organization.getId(), versionControlSystemAdapter.getName(),
                    contentName)) {
                alreadyCollectedCommits = rawStorageAdapter.read(organization.getId(),
                        versionControlSystemAdapter.getName(), contentName);
            }
            alreadyCollectedCommits =
                    versionControlSystemAdapter.getRawCommitsForBranchFromLastCollectionDate(repository.getVcsOrganizationName(),
                            repository.getName(), branchName, lastCollectionDate, alreadyCollectedCommits);
            rawStorageAdapter.save(organization.getId(), versionControlSystemAdapter.getName(), contentName,
                    alreadyCollectedCommits);
            commitsCollected.addAll(versionControlSystemAdapter.commitsBytesToDomain(alreadyCollectedCommits));
        }
        return commitsCollected.stream().toList();
    }

    public List<Tag> collectTagsForOrganizationAndRepository(Organization organization, Repository repository) throws SymeoException {
        final byte[] rawTags =
                versionControlSystemAdapter.getRawTags(organization.getVcsOrganization().getName(),
                        repository.getName());
        rawStorageAdapter.save(
                organization.getId(),
                versionControlSystemAdapter.getName(),
                Tag.getNameFromRepository(repository),
                rawTags);
        return versionControlSystemAdapter.tagsBytesToDomain(rawTags);
    }
}
