package fr.catlean.delivery.processor.domain.command;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;

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

    public void collectRepositoriesForOrganisation(OrganisationAccount organisationAccount) {
        final byte[] rawRepositories =
                versionControlSystemAdapter.getRawRepositories(organisationAccount.getVcsConfiguration().getOrganisationName());
        rawStorageAdapter.save(
                organisationAccount.getVcsConfiguration().getOrganisationName(),
                versionControlSystemAdapter.getName(),
                Repository.ALL,
                rawRepositories);
    }

    public void collectPullRequestsForRepository(Repository repository) {
        byte[] alreadyRawPullRequestsCollected = null;
        if (rawStorageAdapter.exists(repository.getOrganisationName(), versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()))) {
            alreadyRawPullRequestsCollected = rawStorageAdapter.read(repository.getOrganisationName(),
                    versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()));
        }
        final byte[] rawPullRequestsForRepository =
                versionControlSystemAdapter.getRawPullRequestsForRepository(repository,
                        alreadyRawPullRequestsCollected);
        rawStorageAdapter.save(
                repository.getOrganisationName(),
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()),
                rawPullRequestsForRepository);
    }
}
