package io.symeo.monolithic.backend.domain.command;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DeliveryCommandTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_collect_all_repositories_given_an_organization() throws SymeoException {
        // Given
        final String organizationName = faker.pokemon().name();
        final String vcsName = faker.pokemon().location();
        final UUID organizationId = UUID.randomUUID();
        final Organization organization = Organization.builder()
                .name(organizationName)
                .id(organizationId)
                .vcsOrganization(VcsOrganization.builder().name(vcsName).build())
                .build();
        final String vcsAdapterName = faker.animal().name();
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] bytes = new byte[0];

        // When
        when(rawStorageAdapter.exists(organizationId, vcsAdapterName, Repository.ALL)).thenReturn(false);
        when(versionControlSystemAdapter.getRawRepositories(vcsName)).thenReturn(bytes);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        deliveryCommand.collectRepositoriesForOrganization(organization);

        // Then
        verify(rawStorageAdapter, times(1)).save(organizationId, vcsAdapterName, Repository.ALL, bytes);
    }


    @Test
    void should_collect_all_pull_requests_given_a_repository_and_no_already_collected_pull_requests() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final UUID organizationId = UUID.randomUUID();
        final Repository repository =
                Repository.builder().name(faker.pokemon().name()).id(faker.animal().name()).vcsOrganizationId(
                                faker.rickAndMorty().character()
                        )
                        .organizationId(organizationId)
                        .build();
        final byte[] bytes = new byte[0];

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, null)).thenReturn(bytes);
        when(rawStorageAdapter.exists(repository.getOrganizationId(),
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()))).thenReturn(false);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()), bytes);
    }


    @Test
    void should_collect_updated_all_pull_requests_given_a_repository_with_pull_requests_already_collected() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final UUID organizationId = UUID.randomUUID();
        final Repository repository =
                Repository.builder()
                        .name(faker.pokemon().name())
                        .id(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.rickAndMorty().character())
                        .organizationId(organizationId)
                        .build();
        final byte[] bytes = new byte[0];
        final byte[] pullRequestsBytes = faker.name().name().getBytes();

        // When
        when(versionControlSystemAdapter.getRawPullRequestsForRepository(repository, pullRequestsBytes)).thenReturn(bytes);
        when(rawStorageAdapter.exists(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()))).thenReturn(true);
        when(rawStorageAdapter.read(organizationId, versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repository.getId())))
                .thenReturn(pullRequestsBytes);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()), bytes);
        verify(rawStorageAdapter, times(1)).exists(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepository(repository.getId()));

    }


    @Test
    void should_collect_commits_given_a_pull_request() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final PullRequest pullRequest = PullRequest.builder().number(faker.number().randomDigit()).build();
        final Repository repository =
                Repository.builder().vcsOrganizationName(faker.ancient().hero()).name(faker.pokemon().name()).build();
        final byte[] bytes = faker.ancient().god().getBytes();
        final List<Commit> commits = List.of(Commit.builder().build());

        // When
        when(versionControlSystemAdapter.getRawCommits(repository.getVcsOrganizationName(), repository.getName(),
                pullRequest.getNumber())).thenReturn(
                bytes
        );
        when(versionControlSystemAdapter.commitsBytesToDomain(bytes))
                .thenReturn(commits);
        final List<Commit> commitsResult = deliveryCommand.collectCommitsForPullRequest(repository, pullRequest);

        // Then
        assertThat(commitsResult).isEqualTo(commits);

    }
}
