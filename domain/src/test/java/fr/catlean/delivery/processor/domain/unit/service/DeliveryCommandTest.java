package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.domain.service.DeliveryCommand;
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
        when(versionControlSystemAdapter.getRawRepositories(organisation)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganisation(organisation);

        // Then
        verify(rawStorageAdapter, times(1)).save(organisation, today, vcsAdapterName, Repository.ALL, bytes);
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
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getOrganisationName(), today,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
    }
}
