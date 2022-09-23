package io.symeo.monolithic.backend.domain.command;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.util.Date;
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

    public List<Commit> collectCommitsForForOrganizationAndRepositoryFromLastCollectionDate(final Organization organization,
                                                                                            final Repository repository,
                                                                                            final Date lastCollectionDate) throws SymeoException {

        byte[] alreadyCollectedCommits = null;
        final String contentName = Commit.getNameFromRepository(repository);
        if (rawStorageAdapter.exists(organization.getId(), versionControlSystemAdapter.getName(),
                contentName)) {
            alreadyCollectedCommits = rawStorageAdapter.read(organization.getId(),
                    versionControlSystemAdapter.getName(), contentName);
        }
        alreadyCollectedCommits =
                versionControlSystemAdapter.getRawCommitsForRepositoryFromLastCollectionDate(repository.getVcsOrganizationName(),
                        repository.getName(), lastCollectionDate, alreadyCollectedCommits);
        rawStorageAdapter.save(organization.getId(), versionControlSystemAdapter.getName(), contentName,
                alreadyCollectedCommits);
        return versionControlSystemAdapter.commitsBytesToDomain(alreadyCollectedCommits).stream()
                .map(commit -> commit.toBuilder().repositoryId(repository.getId()).build())
                .toList();
    }
}
