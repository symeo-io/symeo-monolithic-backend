package io.symeo.monolithic.backend.domain.command;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
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
                PullRequest.getNameFromRepository(repository.getId()))) {
            alreadyRawPullRequestsCollected = rawStorageAdapter.read(repository.getOrganizationId(),
                    versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()));
        }
        final byte[] rawPullRequestsForRepository =
                versionControlSystemAdapter.getRawPullRequestsForRepository(repository,
                        alreadyRawPullRequestsCollected);
        rawStorageAdapter.save(
                repository.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getId()),
                rawPullRequestsForRepository);
        return versionControlSystemAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
    }

    public List<Commit> collectCommitsForPullRequest(final Repository repository, final PullRequest pullRequest) throws SymeoException {
        final byte[] rawCommits = versionControlSystemAdapter.getRawCommits(repository.getVcsOrganizationName(),
                repository.getName(), pullRequest.getNumber());
        rawStorageAdapter.save(
                pullRequest.getOrganizationId(),
                versionControlSystemAdapter.getName(),
                Commit.getNameFromPullRequest(pullRequest),
                rawCommits);
        return versionControlSystemAdapter.commitsBytesToDomain(rawCommits);
    }
}
