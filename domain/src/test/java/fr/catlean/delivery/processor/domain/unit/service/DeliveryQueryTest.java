package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.domain.service.DeliveryQuery;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeliveryQueryTest {

    private final Faker faker = Faker.instance();
    final String dateAsString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

    @Test
    void should_read_repositories_given_an_organisation() {
        // Given
        final String organisation = faker.pokemon().name();
        final String contentName = faker.animal().name();

        final VersionControlSystemAdapter versionControlSystemAdapter =
                mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryQuery deliveryQuery = new DeliveryQuery(rawStorageAdapter,
                versionControlSystemAdapter);
        final byte[] dummyBytes = new byte[0];
        final List<Repository> repositoriesStub =
                List.of(Repository.builder().name(faker.harryPotter().character()).build(),
                        Repository.builder().name(faker.harryPotter().book()).build());

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(contentName);
        when(rawStorageAdapter.read(organisation, dateAsString, contentName, Repository.ALL))
                .thenReturn(dummyBytes);
        when(versionControlSystemAdapter.repositoriesBytesToDomain(dummyBytes)).thenReturn(
                repositoriesStub
        );
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganisation(organisation);

        // Then
        assertThat(repositories).isEqualTo(repositoriesStub);
    }

    @Test
    void should_read_pull_requests_given_an_organisation_and_a_repository() {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter =
                mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryQuery deliveryQuery = new DeliveryQuery(rawStorageAdapter,
                versionControlSystemAdapter);
        final String organisationName = faker.name().lastName();
        final String repositoryName = faker.name().firstName();
        final Repository repository =
                Repository.builder().name(repositoryName).organisationName(organisationName).build();
        final byte[] dummyBytes = new byte[0];
        final List<PullRequest> pullRequestsStub = List.of(
                PullRequest.builder().id(1).build(),
                PullRequest.builder().id(2).build(),
                PullRequest.builder().id(3).build()
        );

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(faker.name().username());
        when(rawStorageAdapter.read(organisationName, dateAsString,
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repositoryName))).thenReturn(dummyBytes);
        when(versionControlSystemAdapter.pullRequestsBytesToDomain(dummyBytes)).thenReturn(pullRequestsStub);
        final List<PullRequest> pullRequests =
                deliveryQuery.readPullRequestsForRepository(repository);

        // Then
        assertThat(pullRequests).isEqualTo(pullRequestsStub);

    }
}
