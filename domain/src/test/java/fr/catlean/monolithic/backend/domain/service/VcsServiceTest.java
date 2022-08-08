package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
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
    void should_collect_repositories_given_an_organization() throws CatleanException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String organizationName = faker.name().name();
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().build()).name(organizationName).build();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").vcsOrganizationName(organizationName).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").vcsOrganizationName(organizationName).build();
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
    void should_collect_pull_requests_given_an_organization() throws CatleanException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String organizationName = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .name(organizationName)
                .id(UUID.randomUUID())
                .vcsOrganization(
                        VcsOrganization.builder().name(faker.dragonBall().character()).build()
                )
                .build();
        final Repository repo1 =
                Repository.builder().name(organizationName + "1").vcsOrganizationName(organizationName).build();
        final Repository repo2 =
                Repository.builder().name(organizationName + "2").vcsOrganizationName(organizationName).build();
        final List<Repository> expectedRepositories = List.of(
                repo1,
                repo2
        );

        // When
        when(deliveryQuery.readRepositoriesForOrganization(organization)).thenReturn(expectedRepositories);
        final List<PullRequest> pullRequestList1 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).build(),
                PullRequest.builder().id(faker.animal().name()).build()
        );
        final List<PullRequest> pullRequestList2 = List.of(
                PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).build()
        );

        when(deliveryCommand.collectPullRequestsForRepository(expectedRepositories.get(0)))
                .thenReturn(pullRequestList1);
        when(deliveryCommand.collectPullRequestsForRepository(expectedRepositories.get(1)))
                .thenReturn(pullRequestList2);
        vcsService.collectPullRequestsForOrganization(organization);

        // Then
        final ArgumentCaptor<List<PullRequest>> prArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(expositionStorageAdapter, times(2)).savePullRequestDetails(prArgumentCaptor.capture());
        final List<List<PullRequest>> prArgumentCaptorAllValues = prArgumentCaptor.getAllValues();
        assertThat(prArgumentCaptorAllValues).hasSize(2);
        assertThat(prArgumentCaptorAllValues.get(0)).hasSize(3);
        assertThat(prArgumentCaptorAllValues.get(1)).hasSize(2);
        prArgumentCaptorAllValues.stream().flatMap(Collection::stream)
                .forEach(pullRequest -> assertThat(pullRequest.getOrganizationId()).isEqualTo(organization.getId()));
    }
}
