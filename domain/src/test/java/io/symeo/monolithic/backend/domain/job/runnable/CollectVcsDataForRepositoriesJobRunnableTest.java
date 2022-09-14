package io.symeo.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CollectVcsDataForRepositoriesJobRunnableTest {

    private final Faker faker = new Faker();

    @Test
    void should_collect_vcs_data_given_an_organization_and_repositories() throws SymeoException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final String organisationName = faker.name().username();
        Organization organization = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final CollectVcsDataForRepositoriesJobRunnable collectVcsDataForRepositoriesJobRunnable =
                new CollectVcsDataForRepositoriesJobRunnable(vcsService, organization);
        final List<Repository> repositories = List.of(
                Repository.builder().id(faker.dragonBall().character()).build(),
                Repository.builder().id(faker.rickAndMorty().character()).build()
        );
        final String branch11 = faker.name().firstName();
        final String branch12 = faker.name().lastName();
        final String branch21 = faker.name().title();

        // When
        when(vcsService.collectAllBranchesForOrganizationAndRepository(organization, repositories.get(0)))
                .thenReturn(List.of(branch11, branch12));
        when(vcsService.collectAllBranchesForOrganizationAndRepository(organization, repositories.get(1)))
                .thenReturn(List.of(branch21));
        collectVcsDataForRepositoriesJobRunnable.run(repositories.stream().map(Task::newTaskForInput).toList());

        // Then
        verify(vcsService, times(1)).collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repositories.get(0));
        verify(vcsService, times(1)).collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repositories.get(1));
        verify(vcsService, times(1)).collectCommitsForForOrganizationAndRepositoryAndBranch(organization,
                repositories.get(0), branch11);
        verify(vcsService, times(1)).collectCommitsForForOrganizationAndRepositoryAndBranch(organization,
                repositories.get(0), branch12);
        verify(vcsService, times(1)).collectCommitsForForOrganizationAndRepositoryAndBranch(organization,
                repositories.get(1), branch21);
    }

}
