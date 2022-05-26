package fr.catlean.delivery.processor.domain.unit.command;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.*;

public class DeliveryCommandTest {

    private final Faker faker = Faker.instance();
    final String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());


    @Test
    void should_collect_all_repositories_given_an_organisation() {
        // Given
        final String organisation = faker.pokemon().name();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(rawStorageAdapter.exists(organisation, today, vcsAdapterName, Repository.ALL)).thenReturn(false);
        when(versionControlSystemAdapter.getRawRepositories(organisation)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganisation(organisation);

        // Then
        verify(rawStorageAdapter, times(1)).save(organisation, today, vcsAdapterName, Repository.ALL, bytes);
    }

    @Test
    void should_not_collect_all_repositories_given_an_organisation_with_repositories_already_collected() {
        // Given
        final String organisation = faker.pokemon().name();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        when(rawStorageAdapter.exists(organisation, today, vcsAdapterName, Repository.ALL)).thenReturn(true);
        deliveryCommand.collectRepositoriesForOrganisation(organisation);

        // Then
        verify(rawStorageAdapter, times(1)).exists(organisation, today, vcsAdapterName, Repository.ALL);
        verify(rawStorageAdapter, times(0)).save(organisation, today, vcsAdapterName, Repository.ALL, bytes);
    }


    @Test
    void should_collect_all_pull_requests_given_a_repository() {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).organisationName(faker.name().firstName()).build();
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getOrganisationName(), today,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()))).thenReturn(false);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getOrganisationName(), today,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
    }


    @Test
    void should_not_collect_all_pull_requests_given_a_repository_with_pull_requests_already_collected() {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).organisationName(faker.name().firstName()).build();
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getOrganisationName(), today,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()))).thenReturn(true);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(0)).save(repository.getOrganisationName(), today,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
        verify(rawStorageAdapter, times(1)).exists(repository.getOrganisationName(), today,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()));
    }




}
