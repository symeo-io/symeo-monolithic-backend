package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeliveryQueryTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_read_repositories_given_an_organization() throws CatleanException {
        // Given
        final String organizationName = faker.pokemon().name();
        final Organization organization =
                Organization.builder().id(UUID.randomUUID()).name(organizationName).vcsOrganization(
                        VcsOrganization.builder().name(faker.rickAndMorty().character()).build()
                ).build();
        final String contentName = faker.animal().name();

        final VersionControlSystemAdapter versionControlSystemAdapter =
                mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryQuery deliveryQuery = new DeliveryQuery(rawStorageAdapter,
                versionControlSystemAdapter);
        final byte[] dummyBytes = new byte[0];
        final List<Repository> repositoriesStub =
                List.of(Repository.builder().name(faker.harryPotter().character()).id(faker.animal().name()).build(),
                        Repository.builder().name(faker.harryPotter().book()).id(faker.pokemon().name()).build());

        // When
        when(versionControlSystemAdapter.getName()).thenReturn(contentName);
        when(rawStorageAdapter.read(organization.getId(), contentName, Repository.ALL))
                .thenReturn(dummyBytes);
        when(versionControlSystemAdapter.repositoriesBytesToDomain(dummyBytes)).thenReturn(
                repositoriesStub
        );
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganization(organization);

        // Then
        assertThat(repositories).isEqualTo(repositoriesStub);
    }

}
