package fr.catlean.delivery.processor.domain.query;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganizationAccount;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;

import java.util.List;

public class DeliveryQuery {

    private final RawStorageAdapter rawStorageAdapter;
    private final VersionControlSystemAdapter versionControlSystemAdapter;

    public DeliveryQuery(RawStorageAdapter rawStorageAdapter,
                         VersionControlSystemAdapter versionControlSystemAdapter) {
        this.rawStorageAdapter = rawStorageAdapter;
        this.versionControlSystemAdapter = versionControlSystemAdapter;
    }

    public List<Repository> readRepositoriesForOrganization(OrganizationAccount organizationAccount) {
        final byte[] repositoriesBytes =
                rawStorageAdapter.read(organizationAccount.getVcsConfiguration().getOrganizationName(),
                versionControlSystemAdapter.getName(), Repository.ALL);
        return versionControlSystemAdapter.repositoriesBytesToDomain(repositoriesBytes);
    }

    public List<PullRequest> readPullRequestsForRepository(Repository repository) {
        final byte[] pullRequestsBytes = rawStorageAdapter.read(repository.getOrganizationName(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()));
        return versionControlSystemAdapter.pullRequestsBytesToDomain(pullRequestsBytes);
    }
}
