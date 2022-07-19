package fr.catlean.monolithic.backend.domain.command;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.text.SimpleDateFormat;

public class DeliveryCommand {

    private final RawStorageAdapter rawStorageAdapter;
    private final VersionControlSystemAdapter versionControlSystemAdapter;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    public DeliveryCommand(
            RawStorageAdapter rawStorageAdapter,
            VersionControlSystemAdapter versionControlSystemAdapter) {
        this.rawStorageAdapter = rawStorageAdapter;
        this.versionControlSystemAdapter = versionControlSystemAdapter;
    }

    public void collectRepositoriesForOrganization(Organization organizationAccount) throws CatleanException {
        final byte[] rawRepositories =
                versionControlSystemAdapter.getRawRepositories(organizationAccount.getVcsConfiguration().getOrganizationName());
        rawStorageAdapter.save(
                organizationAccount.getVcsConfiguration().getOrganizationName(),
                versionControlSystemAdapter.getName(),
                Repository.ALL,
                rawRepositories);
    }

    public void collectPullRequestsForRepository(Repository repository) throws CatleanException {
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
    }
}
