package io.symeo.monolithic.backend.domain.command;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
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
    void should_collect_all_pull_requests_given_a_repository_and_not_already_collected_pull_requests() throws SymeoException {
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
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepositoryId(repository.getId()))).thenReturn(false);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepositoryId(repository.getId()), bytes);
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
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepositoryId(repository.getId()))).thenReturn(true);
        when(rawStorageAdapter.read(organizationId, versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepositoryId(repository.getId())))
                .thenReturn(pullRequestsBytes);
        deliveryCommand.collectPullRequestsForRepository(repository);

        // Then
        verify(rawStorageAdapter, times(1)).save(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepositoryId(repository.getId()), bytes);
        verify(rawStorageAdapter, times(1)).exists(organizationId,
                versionControlSystemAdapter.getName(), PullRequest.getNameFromRepositoryId(repository.getId()));

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
        when(versionControlSystemAdapter.getRawCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(),
                pullRequest.getNumber())).thenReturn(
                bytes
        );
        when(versionControlSystemAdapter.commitsBytesToDomain(bytes))
                .thenReturn(commits);
        final List<Commit> commitsResult = deliveryCommand.collectCommitsForPullRequest(repository, pullRequest);

        // Then
        assertThat(commitsResult).isEqualTo(commits);
    }

    @Test
    void should_collect_comments_given_a_pull_request() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final PullRequest pullRequest = PullRequest.builder().number(faker.number().randomDigit()).build();
        final Repository repository =
                Repository.builder().vcsOrganizationName(faker.ancient().hero()).name(faker.pokemon().name()).build();
        final byte[] bytes = faker.ancient().god().getBytes();
        final List<Comment> comments = List.of(Comment.builder().build());

        // When
        when(versionControlSystemAdapter.getRawComments(repository.getVcsOrganizationName(), repository.getName(),
                pullRequest.getNumber())).thenReturn(
                bytes
        );
        when(versionControlSystemAdapter.commentsBytesToDomain(bytes))
                .thenReturn(comments);
        final List<Comment> commentsResult = deliveryCommand.collectCommentsForRepositoryAndPullRequest(repository,
                pullRequest);

        // Then
        assertThat(commentsResult).isEqualTo(comments);
    }


    @Test
    void should_collect_all_commits_given_a_repository_and_not_already_collected_commits() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final String vcsAdapterName = faker.pokemon().name();
        final Repository repository = Repository.builder()
                .name(faker.rickAndMorty().location())
                .id(faker.pokemon().name())
                .vcsOrganizationName(vcsOrganizationName)
                .organizationId(UUID.randomUUID())
                .build();
        final byte[] rawCommits = faker.ancient().god().getBytes();
        final List<Commit> expectedCommits = List.of(
                Commit.builder().sha(faker.gameOfThrones().character()).build(),
                Commit.builder().sha(faker.rickAndMorty().character()).build()
        );

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        when(versionControlSystemAdapter.getRawCommitsForRepository(vcsOrganizationName,
                repository.getName(), null))
                .thenReturn(rawCommits);
        when(versionControlSystemAdapter.commitsBytesToDomain(rawCommits))
                .thenReturn(expectedCommits);
        final List<Commit> commits = deliveryCommand.collectCommitsForRepository(repository);

        // Then
        assertThat(commits).isEqualTo(expectedCommits);
        verify(rawStorageAdapter, times(1))
                .save(repository.getOrganizationId(), vcsAdapterName,
                        Commit.getNameFromRepository(repository), rawCommits);
    }

    @Test
    void should_collect_all_commits_given_a_repository_and_already_collected_commits() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final String vcsAdapterName = faker.pokemon().name();
        final UUID organizationId = UUID.randomUUID();
        final Repository repository = Repository.builder()
                .name(faker.rickAndMorty().location())
                .id(faker.pokemon().name())
                .vcsOrganizationName(vcsOrganizationName)
                .organizationId(organizationId)
                .build();
        final byte[] rawCommits = faker.ancient().god().getBytes();
        final List<Commit> expectedCommits = List.of(
                Commit.builder().sha(faker.gameOfThrones().character()).build(),
                Commit.builder().sha(faker.rickAndMorty().character()).build()
        );
        final byte[] existingRawCommits = faker.ancient().hero().getBytes();

        // When
        when(rawStorageAdapter.exists(organizationId, vcsAdapterName, Commit.getNameFromRepository(repository)))
                .thenReturn(true);
        when(rawStorageAdapter.read(organizationId, vcsAdapterName, Commit.getNameFromRepository(repository)))
                .thenReturn(existingRawCommits);
        when(versionControlSystemAdapter.getName()).thenReturn(vcsAdapterName);
        when(versionControlSystemAdapter.getRawCommitsForRepository(vcsOrganizationName,
                repository.getName(), existingRawCommits))
                .thenReturn(rawCommits);
        when(versionControlSystemAdapter.commitsBytesToDomain(rawCommits))
                .thenReturn(expectedCommits);
        final List<Commit> commits = deliveryCommand.collectCommitsForRepository(repository);

        // Then
        assertThat(commits).isEqualTo(expectedCommits);
        verify(rawStorageAdapter, times(1))
                .save(repository.getOrganizationId(), vcsAdapterName,
                        Commit.getNameFromRepository(repository), rawCommits);
    }

    @Test
    void should_collect_branches_given_an_organization_and_a_repository() throws SymeoException {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryCommand deliveryCommand = new DeliveryCommand(
                rawStorageAdapter, versionControlSystemAdapter
        );
        final String vcsOrganizationName = faker.name().firstName();
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(VcsOrganization.builder().name(vcsOrganizationName).build())
                .name(vcsOrganizationName)
                .build();
        final Repository repository = Repository.builder().organizationId(organization.getId())
                .vcsOrganizationName(vcsOrganizationName)
                .id(faker.rickAndMorty().character())
                .build();
        final List<Branch> branches = List.of(
                Branch.builder().name(faker.pokemon().name()).build(),
                Branch.builder().name(faker.pokemon().location()).build()
        );
        final byte[] bytes = faker.ancient().hero().getBytes();

        // When
        when(versionControlSystemAdapter.getRawBranches(organization.getVcsOrganization().getName(),
                repository.getName()))
                .thenReturn(
                        bytes
                );
        when(versionControlSystemAdapter.branchesBytesToDomain(bytes)).thenReturn(branches);
        final List<Branch> branchListResult = deliveryCommand.collectBranchesForOrganizationAndRepository(organization,
                repository);

        // Then
        verify(rawStorageAdapter, times(1))
                .save(
                        organization.getId(),
                        versionControlSystemAdapter.getName(),
                        Branch.ALL,
                        bytes
                );
        assertThat(branchListResult).isEqualTo(branches);
    }
}
