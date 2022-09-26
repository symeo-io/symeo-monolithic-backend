package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class VcsServiceTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_collect_repositories_given_an_organization() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand, expositionStorageAdapter);
        final Organization organization = Organization.builder()
                .vcsOrganization(
                        VcsOrganization.builder()
                                .vcsId(faker.animal().name())
                                .name(faker.funnyName().name())
                                .build())
                .name(faker.name().firstName()).build();
        final List<Repository> repositories = List.of(
                Repository.builder().id(faker.rickAndMorty().character()).build(),
                Repository.builder().id(faker.rickAndMorty().location()).build()
        );

        // When
        when(deliveryCommand.collectRepositoriesForOrganization(organization))
                .thenReturn(
                        repositories
                );
        vcsService.collectRepositoriesForOrganization(organization);

        // Then
        verify(deliveryCommand, times(1)).collectRepositoriesForOrganization(organization);
        final ArgumentCaptor<List<Repository>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(1)).saveRepositories(listArgumentCaptor.capture());
        final List<Repository> argumentCaptorValue = listArgumentCaptor.getValue();
        assertThat(argumentCaptorValue).hasSize(repositories.size());
        for (Repository repository : argumentCaptorValue) {
            assertThat(repository.getOrganizationId()).isEqualTo(organization.getId());
            assertThat(repository.getVcsOrganizationName()).isEqualTo(organization.getVcsOrganization().getName());
            assertThat(repository.getVcsOrganizationId()).isEqualTo(organization.getVcsOrganization().getVcsId());
        }

    }

    @Test
    void should_raise_an_exception_while_collection_pull_requests() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand, expositionStorageAdapter);
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().build()).name(faker.name().firstName()).build();

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
    void should_collect_organization_data_given_an_organization_a_repository_and_last_collection_date() throws SymeoException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand, expositionStorageAdapter);
        final String vcsOrganizationId = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .vcsOrganization(
                        VcsOrganization.builder().name(faker.dragonBall().character())
                                .id(faker.rickAndMorty().location())
                                .vcsId(faker.animal().name())
                                .build()
                )
                .build();
        final Repository repo1 =
                Repository.builder().id(faker.pokemon().name()).name(vcsOrganizationId + "1")
                        .vcsOrganizationId(vcsOrganizationId + "id-1").build();
        final Date lastCollectionDate1 = DateHelper.stringToDate("2020-01-01");
        final List<PullRequest> pullRequestList1 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).number(11).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).number(12).build(),
                PullRequest.builder().id(faker.animal().name()).number(13).build()
        );
        final List<Branch> branches =
                Stream.of(faker.name().firstName(), faker.name().lastName()).map(s -> Branch.builder().name(s).build()).toList();
        final List<Commit> commits = List.of(Commit.builder().sha(faker.rickAndMorty().character()).build());
        final List<Tag> tags = List.of(
                Tag.builder().commitSha(faker.ancient().god()).build(),
                Tag.builder().commitSha(faker.ancient().hero()).build()
        );


        // When
        final Repository updatedRepository = repo1.toBuilder().organizationId(organization.getId()).build();
        when(deliveryCommand.collectPullRequestsForRepository(updatedRepository))
                .thenReturn(pullRequestList1);
        when(deliveryCommand.collectBranchesForOrganizationAndRepository(organization, updatedRepository))
                .thenReturn(branches);
        when(deliveryCommand.collectCommitsForForOrganizationAndRepositoryAndBranchesFromLastCollectionDate(organization, updatedRepository,
                branches.stream().map(Branch::getName).toList(), lastCollectionDate1))
                .thenReturn(
                        commits
                );
        when(deliveryCommand.collectTagsForOrganizationAndRepository(organization, updatedRepository))
                .thenReturn(tags);
        vcsService.collectVcsDataForOrganizationAndRepositoryFromLastCollectionDate(organization, updatedRepository,
                lastCollectionDate1);

        // Then
        final ArgumentCaptor<List<PullRequest>> prArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(1)).savePullRequestDetailsWithLinkedComments(prArgumentCaptor.capture());
        final List<PullRequest> prArgumentCaptorValue = prArgumentCaptor.getValue();
        assertThat(prArgumentCaptorValue).hasSize(3);
        prArgumentCaptorValue
                .forEach(pullRequest -> {
                    assertThat(pullRequest.getOrganizationId()).isEqualTo(organization.getId());
                    assertThat(pullRequest.getVcsOrganizationId()).isEqualTo(repo1.getVcsOrganizationId());
                });
        final ArgumentCaptor<List<Commit>> commitsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(1)).saveCommits(commitsArgumentCaptor.capture());
        assertThat(commitsArgumentCaptor.getValue()).hasSize(commits.size());
        for (Commit commit : commitsArgumentCaptor.getValue()) {
            assertThat(commit.getRepositoryId()).isEqualTo(repo1.getId());
        }
        final ArgumentCaptor<List<Tag>> tagsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(1)).saveTags(tagsArgumentCaptor.capture());
        assertThat(tagsArgumentCaptor.getValue()).hasSize(tags.size());
        for (Tag tag : tagsArgumentCaptor.getValue()) {
            assertThat(tag.getRepository()).isEqualTo(updatedRepository);
        }
    }

}