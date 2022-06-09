package fr.catlean.delivery.processor.domain.query;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
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

    public List<Repository> readRepositoriesForOrganisation(OrganisationAccount organisationAccount) {
        final byte[] repositoriesBytes =
                rawStorageAdapter.read(organisationAccount.getVcsConfiguration().getOrganisationName(),
                versionControlSystemAdapter.getName(), Repository.ALL);
        return versionControlSystemAdapter.repositoriesBytesToDomain(repositoriesBytes);
    }

    public List<PullRequest> readPullRequestsForRepository(Repository repository) {
        final byte[] pullRequestsBytes = rawStorageAdapter.read(repository.getOrganisationName(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()));
        return versionControlSystemAdapter.pullRequestsBytesToDomain(pullRequestsBytes);
    }
}
