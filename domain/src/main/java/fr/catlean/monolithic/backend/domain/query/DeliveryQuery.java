package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.util.List;

public class DeliveryQuery {

    private final RawStorageAdapter rawStorageAdapter;
    private final VersionControlSystemAdapter versionControlSystemAdapter;

    public DeliveryQuery(RawStorageAdapter rawStorageAdapter,
                         VersionControlSystemAdapter versionControlSystemAdapter) {
        this.rawStorageAdapter = rawStorageAdapter;
        this.versionControlSystemAdapter = versionControlSystemAdapter;
    }

    public List<Repository> readRepositoriesForOrganization(Organization organization) throws CatleanException {
        final byte[] repositoriesBytes =
                rawStorageAdapter.read(organization.getVcsOrganization().getName(),
                versionControlSystemAdapter.getName(), Repository.ALL);
        return versionControlSystemAdapter.repositoriesBytesToDomain(repositoriesBytes);
    }

    public List<PullRequest> readPullRequestsForRepository(Repository repository) throws CatleanException {
        final byte[] pullRequestsBytes = rawStorageAdapter.read(repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()));
        return versionControlSystemAdapter.pullRequestsBytesToDomain(pullRequestsBytes);
    }
}
