package fr.catlean.monolithic.backend.domain.command;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

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

    public List<Repository> collectRepositoriesForOrganization(Organization organization) throws CatleanException {
        final byte[] rawRepositories =
                versionControlSystemAdapter.getRawRepositories(organization.getVcsOrganization().getName());
        rawStorageAdapter.save(
                organization.getVcsOrganization().getName(),
                versionControlSystemAdapter.getName(),
                Repository.ALL,
                rawRepositories);
        return versionControlSystemAdapter.repositoriesBytesToDomain(rawRepositories);
    }

    public List<PullRequest> collectPullRequestsForRepository(Repository repository) throws CatleanException {
        byte[] alreadyRawPullRequestsCollected = null;
        if (rawStorageAdapter.exists(repository.getVcsOrganizationName(), versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()))) {
            alreadyRawPullRequestsCollected = rawStorageAdapter.read(repository.getVcsOrganizationName(),
                    versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()));
        }
        final byte[] rawPullRequestsForRepository =
                versionControlSystemAdapter.getRawPullRequestsForRepository(repository,
                        alreadyRawPullRequestsCollected);
        rawStorageAdapter.save(
                repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()),
                rawPullRequestsForRepository);
        return versionControlSystemAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
    }
}
