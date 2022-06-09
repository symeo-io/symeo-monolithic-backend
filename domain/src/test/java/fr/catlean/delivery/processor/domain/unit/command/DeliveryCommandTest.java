package fr.catlean.delivery.processor.domain.unit.command;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class DeliveryCommandTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_collect_all_repositories_given_an_organisation() {
        // Given
        final String organisationName = faker.pokemon().name();
        final String vcsOrganisationName = faker.pokemon().location();
        final OrganisationAccount organisationAccount = OrganisationAccount.builder()
                .name(organisationName)
                .vcsConfiguration(VcsConfiguration.builder().organisationName(vcsOrganisationName).build())
                .build();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(rawStorageAdapter.exists(vcsOrganisationName, vcsAdapterName, Repository.ALL)).thenReturn(false);
        when(versionControlSystemAdapter.getRawRepositories(vcsOrganisationName)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganisation(organisationAccount);

        // Then
        verify(rawStorageAdapter, times(1)).save(vcsOrganisationName, vcsAdapterName, Repository.ALL, bytes);
    }


    @Test
    void should_collect_all_pull_requests_given_a_repository_and_no_already_collected_pull_requests() {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).organisationName(faker.name().firstName()).build();
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, null)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getOrganisationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()))).thenReturn(false);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getOrganisationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
    }


    @Test
    void should_collect_updated_all_pull_requests_given_a_repository_with_pull_requests_already_collected() {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).organisationName(faker.name().firstName()).build();
        final byte[] bytes = new byte[0];
        final byte[] pullRequestsBytes = faker.name().name().getBytes();

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, pullRequestsBytes)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getOrganisationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()))).thenReturn(true);
        when(rawStorageAdapter.read(repository.getOrganisationName(), versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName())))
                .thenReturn(pullRequestsBytes);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getOrganisationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
        verify(rawStorageAdapter, times(1)).exists(repository.getOrganisationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()));

    }


}
