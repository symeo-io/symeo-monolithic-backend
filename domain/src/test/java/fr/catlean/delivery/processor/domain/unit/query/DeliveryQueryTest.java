package fr.catlean.delivery.processor.domain.unit.query;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganizationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.domain.query.DeliveryQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeliveryQueryTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_read_repositories_given_an_organization() {
        // Given
        final String organizationName = faker.pokemon().name();
        final String vcsOrganizationName = faker.harryPotter().book();
        final OrganizationAccount organizationAccount =
                OrganizationAccount.builder().name(organizationName).vcsConfiguration(
                        VcsConfiguration.builder().organizationName(vcsOrganizationName).build()
                ).build();
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
        when(rawStorageAdapter.read(vcsOrganizationName, contentName, Repository.ALL))
                .thenReturn(dummyBytes);
        when(versionControlSystemAdapter.repositoriesBytesToDomain(dummyBytes)).thenReturn(
                repositoriesStub
        );
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganization(organizationAccount);

        // Then
        assertThat(repositories).isEqualTo(repositoriesStub);
    }

    @Test
    void should_read_pull_requests_given_an_organization_and_a_repository() {
        // Given
        final VersionControlSystemAdapter versionControlSystemAdapter =
                mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryQuery deliveryQuery = new DeliveryQuery(rawStorageAdapter,
                versionControlSystemAdapter);
        final String organizationName = faker.name().lastName();
        final String repositoryName = faker.name().firstName();
        final Repository repository =
                Repository.builder().name(repositoryName).organizationName(organizationName).build();
        final byte[] dummyBytes = new byte[0];
        final List<PullRequest> pullRequestsStub = List.of(
                PullRequest.builder().id("github-1").build(),
                PullRequest.builder().id("github-2").build(),
                PullRequest.builder().id("github-3").build()
        );

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(faker.name().username());
        when(rawStorageAdapter.read(organizationName,
                versionControlSystemAdapter.getName(),
                PullRequest.getNameFromRepository(repositoryName))).thenReturn(dummyBytes);
        when(versionControlSystemAdapter.pullRequestsBytesToDomain(dummyBytes)).thenReturn(pullRequestsStub);
        final List<PullRequest> pullRequests =
                deliveryQuery.readPullRequestsForRepository(repository);

        // Then
        assertThat(pullRequests).isEqualTo(pullRequestsStub);

    }
}
