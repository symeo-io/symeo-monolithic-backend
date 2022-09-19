package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.query.DeliveryQuery;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class VcsServiceTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_collect_repositories_given_an_organization() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String vcsOrganizationId = faker.name().name();
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().build()).name(faker.name().firstName()).build();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").vcsOrganizationId(vcsOrganizationId).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").vcsOrganizationId(vcsOrganizationId).build();
        final List<Repository> expectedRepositories = List.of(
                repo1,
                repo2
        );
        when(deliveryQuery.readRepositoriesForOrganization(organization))
                .thenReturn(
                        expectedRepositories
                );
        vcsService.collectRepositoriesForOrganization(organization);

        // Then
        verify(deliveryCommand, times(1)).collectRepositoriesForOrganization(organization);
    }

    @Test
    void should_raise_an_exception_while_collection_pull_requests() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String vcsOrganizationId = faker.name().name();
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().build()).name(faker.name().firstName()).build();
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").vcsOrganizationId(vcsOrganizationId).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").vcsOrganizationId(vcsOrganizationId).build();
        final List<Repository> expectedRepositories = List.of(
                repo1,
                repo2
        );
        when(deliveryQuery.readRepositoriesForOrganization(organization))
                .thenReturn(
                        expectedRepositories
                );

        // When
        doThrow(SymeoException.class)
                .when(deliveryCommand)
                .collectRepositoriesForOrganization(any());
        SymeoException symeoException = null;
        try {
            vcsService.collectRepositoriesForOrganization(organization);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
    }

    @Test
    void should_collect_pull_requests_with_commits_given_an_organization() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String vcsOrganizationId = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .vcsOrganization(
                        VcsOrganization.builder().name(faker.dragonBall().character()).build()
                )
                .build();
        final Repository repo1 =
                Repository.builder().id(faker.pokemon().name()).name(vcsOrganizationId + "1")
                        .vcsOrganizationId(vcsOrganizationId + "id-1").build();
        final Repository repo2 =
                Repository.builder().id(faker.rickAndMorty().character()).name(vcsOrganizationId + "2")
                        .vcsOrganizationId(vcsOrganizationId + "id-2").build();
        final List<Repository> expectedRepositories = List.of(
                repo1,
                repo2
        );

        // When
        when(deliveryQuery.readRepositoriesForOrganization(organization)).thenReturn(expectedRepositories);
        final List<PullRequest> pullRequestList1 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).number(11).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).number(12).build(),
                PullRequest.builder().id(faker.animal().name()).number(13).build()
        );
        final List<PullRequest> pullRequestList2 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).number(21).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).number(22).build()
        );
        vcsService.collectVcsDataForOrganizationAndRepository(organization, repo1);
        vcsService.collectVcsDataForOrganizationAndRepository(organization, repo2);

        when(deliveryCommand.collectPullRequestsForRepository(expectedRepositories.get(0)))
                .thenReturn(pullRequestList1);
        when(deliveryCommand.collectPullRequestsForRepository(expectedRepositories.get(1)))
                .thenReturn(pullRequestList2);
        when(deliveryCommand.collectCommitsForPullRequest(repo1, pullRequestList1.get(0)))
                .thenReturn(List.of(Commit.builder().build()));
        when(deliveryCommand.collectCommitsForPullRequest(repo1, pullRequestList1.get(1)))
                .thenReturn(List.of(Commit.builder().build()));
        when(deliveryCommand.collectCommitsForPullRequest(repo1, pullRequestList1.get(2)))
                .thenReturn(List.of(Commit.builder().build()));
        when(deliveryCommand.collectCommitsForPullRequest(repo2, pullRequestList2.get(0)))
                .thenReturn(List.of(Commit.builder().build()));
        when(deliveryCommand.collectCommitsForPullRequest(repo2, pullRequestList2.get(1)))
                .thenReturn(List.of(Commit.builder().build()));

//        vcsService.collectPullRequestsForOrganization(organization);

        // Then
        final ArgumentCaptor<List<PullRequest>> prArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(2)).savePullRequestDetailsWithLinkedCommitsAndComments(prArgumentCaptor.capture());
        final List<List<PullRequest>> prArgumentCaptorAllValues = prArgumentCaptor.getAllValues();
        assertThat(prArgumentCaptorAllValues).hasSize(2);
        prArgumentCaptorAllValues.stream().flatMap(Collection::stream)
                .forEach(pullRequest -> {
                    assertThat(pullRequest.getOrganizationId()).isEqualTo(organization.getId());
                    assertThat(pullRequest.getCommits()).isNotEmpty();
                });
    }

    @Test
    void should_collect_pull_requests_with_comments_given_an_organization() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String vcsOrganizationId = faker.pokemon().name();
        final VcsOrganization vcsOrganization =
                VcsOrganization.builder().name(faker.dragonBall().character()).vcsId(vcsOrganizationId).build();
        final Organization organization = Organization.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .vcsOrganization(
                        vcsOrganization
                )
                .build();
        final Repository repo1 =
                Repository.builder().id(faker.pokemon().name()).name(vcsOrganizationId + "1")
                        .vcsOrganizationId(vcsOrganizationId + "id-1").build();
        final Repository repo2 =
                Repository.builder().id(faker.rickAndMorty().character()).name(vcsOrganizationId + "2")
                        .vcsOrganizationId(vcsOrganizationId + "id-2").build();
        final List<Repository> expectedRepositories = List.of(
                repo1,
                repo2
        );

        // When
        when(deliveryQuery.readRepositoriesForOrganization(organization)).thenReturn(expectedRepositories);
        final List<PullRequest> pullRequestList1 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).number(11).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).number(12).build(),
                PullRequest.builder().id(faker.animal().name()).number(13).build()
        );
        final List<PullRequest> pullRequestList2 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).number(21).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).number(22).build()
        );

        when(deliveryCommand.collectPullRequestsForRepository(expectedRepositories.get(0)))
                .thenReturn(pullRequestList1);
        when(deliveryCommand.collectPullRequestsForRepository(expectedRepositories.get(1)))
                .thenReturn(pullRequestList2);
        when(deliveryCommand.collectCommentsForRepositoryAndPullRequest(repo1, pullRequestList1.get(0)
                .toBuilder().organizationId(organization.getId()).vcsOrganizationId(vcsOrganizationId).build()))
                .thenReturn(List.of(Comment.builder().build()));
        when(deliveryCommand.collectCommentsForRepositoryAndPullRequest(repo1, pullRequestList1.get(1)
                .toBuilder().organizationId(organization.getId()).vcsOrganizationId(vcsOrganizationId).build()))
                .thenReturn(List.of(Comment.builder().build()));
        when(deliveryCommand.collectCommentsForRepositoryAndPullRequest(repo1, pullRequestList1.get(2)
                .toBuilder().organizationId(organization.getId()).vcsOrganizationId(vcsOrganizationId).build()))
                .thenReturn(List.of(Comment.builder().build()));
        when(deliveryCommand.collectCommentsForRepositoryAndPullRequest(repo2, pullRequestList2.get(0)
                .toBuilder().organizationId(organization.getId()).vcsOrganizationId(vcsOrganizationId).build()))
                .thenReturn(List.of(Comment.builder().build()));
        when(deliveryCommand.collectCommentsForRepositoryAndPullRequest(repo2, pullRequestList2.get(1)
                .toBuilder().organizationId(organization.getId()).vcsOrganizationId(vcsOrganizationId).build()))
                .thenReturn(List.of(Comment.builder().build()));
        vcsService.collectVcsDataForOrganizationAndRepository(organization, repo1);
        vcsService.collectVcsDataForOrganizationAndRepository(organization, repo2);

        // Then
        final ArgumentCaptor<List<PullRequest>> prArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(2)).savePullRequestDetailsWithLinkedCommitsAndComments(prArgumentCaptor.capture());
        final List<List<PullRequest>> prArgumentCaptorAllValues = prArgumentCaptor.getAllValues();
        assertThat(prArgumentCaptorAllValues).hasSize(2);
        prArgumentCaptorAllValues.stream().flatMap(Collection::stream)
                .forEach(pullRequest -> {
                    assertThat(pullRequest.getOrganizationId()).isEqualTo(organization.getId());
                    assertThat(pullRequest.getVcsOrganizationId()).isEqualTo(vcsOrganizationId);
                });
    }
}
