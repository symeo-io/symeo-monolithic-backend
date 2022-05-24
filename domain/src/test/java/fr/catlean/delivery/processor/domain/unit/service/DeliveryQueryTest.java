package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.domain.service.DeliveryQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeliveryQueryTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_read_repositories_given_an_organisation() {
        // Given
        final String organisation = faker.pokemon().name();
        final String contentName = faker.animal().name();
        final String dateAsString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        final VersionControlSystemAdapter versionControlSystemAdapter = mock(VersionControlSystemAdapter.class);
        final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
        final DeliveryQuery deliveryQuery = new DeliveryQuery(rawStorageAdapter, versionControlSystemAdapter);
        final byte[] dummyBytes = new byte[0];
        final List<Repository> repositoriesStub = List.of(Repository.builder().name(faker.harryPotter().character()).build(),
                Repository.builder().name(faker.harryPotter().book()).build());


        // When
        when(versionControlSystemAdapter.getName()).thenReturn(contentName);
        when(rawStorageAdapter.read(organisation, dateAsString, contentName, "get_repositories"))
                .thenReturn(dummyBytes);
        when(versionControlSystemAdapter.repositoriesBytesToDomain(dummyBytes)).thenReturn(
                repositoriesStub
        );
        List<Repository> repositories = deliveryQuery.readRepositoriesForOrganisation(organisation);

        // Then
        Assertions.assertThat(repositories).isEqualTo(repositoriesStub);
    }
}
