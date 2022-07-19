package fr.catlean.monolithic.backend.domain.command;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class DeliveryCommandTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_collect_all_repositories_given_an_organization() throws CatleanException {
        // Given
        final String organizationName = faker.pokemon().name();
        final String vcsOrganizationName = faker.pokemon().location();
        final Organization organizationAccount = Organization.builder()
                .name(organizationName)
                .vcsConfiguration(VcsConfiguration.builder().organizationName(vcsOrganizationName).build())
                .build();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(rawStorageAdapter.exists(vcsOrganizationName, vcsAdapterName, Repository.ALL)).thenReturn(false);
        when(versionControlSystemAdapter.getRawRepositories(vcsOrganizationName)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganization(organizationAccount);

        // Then
        verify(rawStorageAdapter, times(1)).save(vcsOrganizationName, vcsAdapterName, Repository.ALL, bytes);
    }


    @Test
    void should_collect_all_pull_requests_given_a_repository_and_no_already_collected_pull_requests() throws CatleanException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).vcsOrganizationName(faker.name().firstName()).build();
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, null)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()))).thenReturn(false);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
    }


    @Test
    void should_collect_updated_all_pull_requests_given_a_repository_with_pull_requests_already_collected() throws CatleanException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).vcsOrganizationName(faker.name().firstName()).build();
        final byte[] bytes = new byte[0];
        final byte[] pullRequestsBytes = faker.name().name().getBytes();

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, pullRequestsBytes)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()))).thenReturn(true);
        when(rawStorageAdapter.read(repository.getVcsOrganizationName(), versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getName())))
                .thenReturn(pullRequestsBytes);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()), bytes);
        verify(rawStorageAdapter, times(1)).exists(repository.getVcsOrganizationName(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getName()));

    }


}
