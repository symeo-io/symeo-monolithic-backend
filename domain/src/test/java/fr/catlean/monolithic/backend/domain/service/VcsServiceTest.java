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
    void should_raise_an_exception() throws CatleanException {
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
        doThrow(CatleanException.class)
                .when(deliveryCommand)
                .collectRepositoriesForOrganization(any());
        CatleanException catleanException = null;
        try {
            vcsService.collectRepositoriesForOrganization(organization);
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
    }

    @Test
    void should_collect_pull_requests_given_an_organization() throws CatleanException {
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
        prArgumentCaptorAllValues.stream().flatMap(Collection::stream)
                .forEach(pullRequest -> assertThat(pullRequest.getOrganizationId()).isEqualTo(organization.getId()));
    }
}
