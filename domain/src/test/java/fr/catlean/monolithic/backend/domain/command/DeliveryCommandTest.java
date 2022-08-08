package fr.catlean.monolithic.backend.domain.command;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
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
        final String vcsId = faker.rickAndMorty().character();
        final Organization organization = Organization.builder()
                .name(organizationName)
                .vcsOrganization(VcsOrganization.builder().vcsId(vcsId).name(faker.pokemon().location()).build())
                .build();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(rawStorageAdapter.exists(vcsId, vcsAdapterName, Repository.ALL)).thenReturn(false);
        when(versionControlSystemAdapter.getRawRepositories(vcsId)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganization(organization);

        // Then
        verify(rawStorageAdapter, times(1)).save(vcsId, vcsAdapterName, Repository.ALL, bytes);
    }


    @Test
    void should_collect_all_pull_requests_given_a_repository_and_no_already_collected_pull_requests() throws CatleanException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).id(faker.animal().name()).vcsOrganizationId(
                        faker.rickAndMorty().character()
                ).build();
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, null)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()))).thenReturn(false);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()), bytes);
    }


    @Test
    void should_collect_updated_all_pull_requests_given_a_repository_with_pull_requests_already_collected() throws CatleanException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final Repository repository =
                Repository.builder()
                        .name(faker.pokemon().name())
                        .id(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.rickAndMorty().character())
                        .build();
        final byte[] bytes = new byte[0];
        final byte[] pullRequestsBytes = faker.name().name().getBytes();

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, pullRequestsBytes)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()))).thenReturn(true);
        when(rawStorageAdapter.read(repository.getId(), versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getId())))
                .thenReturn(pullRequestsBytes);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()), bytes);
        verify(rawStorageAdapter, times(1)).exists(repository.getVcsOrganizationId(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()));

    }


}
