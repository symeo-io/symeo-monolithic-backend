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
                organization.getVcsOrganization().getVcsId(),
                versionControlSystemAdapter.getName(),
                Repository.ALL,
                rawRepositories);
        return versionControlSystemAdapter.repositoriesBytesToDomain(rawRepositories);
    }

    public List<PullRequest> collectPullRequestsForRepository(Repository repository) throws CatleanException {
        byte[] alreadyRawPullRequestsCollected = null;
        if (rawStorageAdapter.exists(repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getId()))) {
            alreadyRawPullRequestsCollected = rawStorageAdapter.read(repository.getId(),
                    versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()));
        }
        final byte[] rawPullRequestsForRepository =
                versionControlSystemAdapter.getRawPullRequestsForRepository(repository,
                        alreadyRawPullRequestsCollected);
        rawStorageAdapter.save(
                repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getId()),
                rawPullRequestsForRepository);
        return versionControlSystemAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
    }
}
