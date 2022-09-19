package io.symeo.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CollectVcsDataForOrganizationJobRunnableTest {

    private final Faker faker = new Faker();

    @Test
    void should_collect_vcs_data_given_an_organization_and_repositories() throws SymeoException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final String organisationName = faker.name().username();
        Organization organization = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter = mock(AccountOrganizationStorageAdapter.class);
        final CollectVcsDataForOrganizationJobRunnable collectVcsDataForOrganizationJobRunnable =
                new CollectVcsDataForOrganizationJobRunnable(vcsService, organization,
                        expositionStorageAdapter, accountOrganizationStorageAdapter, organization.getId());
        final List<Repository> repositories = List.of(
                Repository.builder().id(faker.dragonBall().character()).build(),
                Repository.builder().id(faker.rickAndMorty().character()).build()
        );
        final String branch11 = faker.name().firstName();
        final String branch12 = faker.name().lastName();
        final String branch21 = faker.name().title();

        // When
        final Repository repository1 = repositories.get(0);
        final Repository repository2 = repositories.get(1);
        final Repository repo1WithOrgId = repository1.toBuilder().organizationId(organization.getId()).build();
        final Repository repo2WithOrgId = repository2.toBuilder().organizationId(organization.getId()).build();
        when(accountOrganizationStorageAdapter.findOrganizationById(organization.getId()))
                .thenReturn(organization);
        when(expositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(organization.getId()))
                .thenReturn(repositories);
        when(vcsService.collectAllBranchesForOrganizationAndRepository(organization, repo1WithOrgId))
                .thenReturn(List.of(branch11, branch12));
        when(vcsService.collectAllBranchesForOrganizationAndRepository(organization, repo2WithOrgId))
                .thenReturn(List.of(branch21));
        collectVcsDataForOrganizationJobRunnable.initializeTasks();
        collectVcsDataForOrganizationJobRunnable.run();

        // Then
        verify(vcsService, times(1)).collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repo1WithOrgId);
        verify(vcsService, times(1)).collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repo2WithOrgId);
        verify(vcsService, times(1)).collectCommitsForForOrganizationAndRepositoryAndBranch(organization,
                repo1WithOrgId, branch11);
        verify(vcsService, times(1)).collectCommitsForForOrganizationAndRepositoryAndBranch(organization,
                repo1WithOrgId, branch12);
        verify(vcsService, times(1)).collectCommitsForForOrganizationAndRepositoryAndBranch(organization,
                repo2WithOrgId, branch21);
    }

}
