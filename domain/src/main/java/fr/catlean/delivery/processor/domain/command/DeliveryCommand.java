package fr.catlean.delivery.processor.domain.command;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public void collectRepositoriesForOrganisation(String organisation) {
        final String today = SDF.format(new Date());
        if (rawStorageAdapter.exists(organisation, today, versionControlSystemAdapter.getName(), Repository.ALL)) {
            return;
        }
        final byte[] rawRepositories = versionControlSystemAdapter.getRawRepositories(organisation);
        rawStorageAdapter.save(
                organisation,
                today,
                versionControlSystemAdapter.getName(),
                Repository.ALL,
                rawRepositories);
    }

    public void collectPullRequestsForRepository(Repository repository) {
        final String today = SDF.format(new Date());
        if (rawStorageAdapter.exists(repository.getOrganisationName(), today, versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()))) {
            return;
        }
        final byte[] rawPullRequestsForRepository =
                versionControlSystemAdapter.getRawPullRequestsForRepository(repository);
        rawStorageAdapter.save(
                repository.getOrganisationName(),
                today,
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName()),
                rawPullRequestsForRepository);
    }
}
