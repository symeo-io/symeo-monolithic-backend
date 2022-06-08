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

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.*;

public class DeliveryCommandTest {

    private final Faker faker = Faker.instance();
    final String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());


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
        when(rawStorageAdapter.exists(vcsOrganisationName, today, vcsAdapterName, Repository.ALL)).thenReturn(false);
        when(versionControlSystemAdapter.getRawRepositories(vcsOrganisationName)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganisation(organisationAccount);

        // Then
        verify(rawStorageAdapter, times(1)).save(vcsOrganisationName, today, vcsAdapterName, Repository.ALL, bytes);
    }

    @Test
    void should_not_collect_all_repositories_given_an_organisation_with_repositories_already_collected() {
        // Given
        final String organisationName = faker.pokemon().name();
        final String vcsOrganisationName = faker.pokemon().location();
        final OrganisationAccount organisationAccount =
                OrganisationAccount.builder().name(organisationName)
                        .vcsConfiguration(VcsConfiguration.builder().organisationName(vcsOrganisationName).build()).
                        build();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        when(rawStorageAdapter.exists(vcsOrganisationName, today, vcsAdapterName, Repository.ALL)).thenReturn(true);
        deliveryCommand.collectRepositoriesForOrganisation(organisationAccount);

        // Then
        verify(rawStorageAdapter, times(1)).exists(vcsOrganisationName, today, vcsAdapterName, Repository.ALL);
        verify(rawStorageAdapter, times(0)).save(vcsOrganisationName, today, vcsAdapterName, Repository.ALL, bytes);
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
