package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.IRepositoryCommitMetrics;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.DataWarehouseAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.domain.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.*;

public class DeliveryServiceTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_start_delivery_processing_by_getting_all_repository_for_a_given_organisation() {
        // Given
        final String organisationName = faker.animal().name();
        final List<Repository> repositories = List.of();
        final VersionControlSystemAdapter versionControlSystemAdapter = Mockito.mock(VersionControlSystemAdapter.class);
        when(versionControlSystemAdapter.getRepositoriesForOrganisationName(organisationName))
                .thenReturn(repositories);
        final DataWarehouseAdapter dataWarehouseAdapter = Mockito.mock(DataWarehouseAdapter.class);
        final DeliveryService deliveryService = new DeliveryService(versionControlSystemAdapter, dataWarehouseAdapter);
        // When
        deliveryService.startProcessing(organisationName);

        // Then
        verify(versionControlSystemAdapter, times(1)).getRepositoriesForOrganisationName(organisationName);
    }


    @Test
    void should_get_commits_metrics_from_vcs_adapter_for_each_repository() {
        // Given
        final String organisationName = faker.animal().name();
        final Repository repository1 = Repository.builder().name(faker.animal().name()).build();
        final IRepositoryCommitMetrics repositoryCommitMetrics1 = new RepositoryCommitMetricsStub();
        final Repository repository2 = Repository.builder().name(faker.pokemon().name()).build();
        final IRepositoryCommitMetrics repositoryCommitMetrics2 = new RepositoryCommitMetricsStub();
        final Repository repository3 = Repository.builder().name(faker.harryPotter().character()).build();
        final IRepositoryCommitMetrics IRepositoryCommitMetrics3 = new RepositoryCommitMetricsStub();
        final List<Repository> repositories = List.of(
                repository1,
                repository2,
                repository3
        );
        final VersionControlSystemAdapter versionControlSystemAdapter = Mockito.mock(VersionControlSystemAdapter.class);
        final DataWarehouseAdapter dataWarehouseAdapter = Mockito.mock(DataWarehouseAdapter.class);
        final DeliveryService deliveryService = new DeliveryService(versionControlSystemAdapter, dataWarehouseAdapter);
        when(versionControlSystemAdapter.getRepositoriesForOrganisationName(organisationName))
                .thenReturn(repositories);
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository1.getName()))
                .thenReturn(repositoryCommitMetrics1);
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository3.getName()))
                .thenReturn(IRepositoryCommitMetrics3);
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository2.getName()))
                .thenReturn(repositoryCommitMetrics2);

        // When
        deliveryService.startProcessing(organisationName);

        // Then
        verify(versionControlSystemAdapter, times(1)).getRepositoriesForOrganisationName(organisationName);
        verify(versionControlSystemAdapter, times(1)).getCommitMetricsForRepository(repository1.getName());
        verify(versionControlSystemAdapter, times(1)).getCommitMetricsForRepository(repository2.getName());
        verify(versionControlSystemAdapter, times(1)).getCommitMetricsForRepository(repository3.getName());
    }


    @Test
    void should_map_commit_metrics_to_domain() {
        // Given
        final String organisationName = faker.animal().name();
        final Repository repository1 = Repository.builder().name(faker.animal().name()).build();
        final IRepositoryCommitMetrics repositoryCommitMetrics1 = new RepositoryCommitMetricsStub();
        final Repository repository2 = Repository.builder().name(faker.pokemon().name()).build();
        final IRepositoryCommitMetrics repositoryCommitMetrics2 = new RepositoryCommitMetricsStub();
        final VersionControlSystemAdapter versionControlSystemAdapter = Mockito.mock(VersionControlSystemAdapter.class);
        final DataWarehouseAdapter dataWarehouseAdapter = Mockito.mock(DataWarehouseAdapter.class);
        final DeliveryService deliveryService = new DeliveryService(versionControlSystemAdapter, dataWarehouseAdapter);
        when(versionControlSystemAdapter.getRepositoriesForOrganisationName(organisationName))
                .thenReturn(List.of(repository1, repository2));
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository1.getName()))
                .thenReturn(repositoryCommitMetrics1);
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository2.getName()))
                .thenReturn(repositoryCommitMetrics2);

        // When
        deliveryService.startProcessing(organisationName);

        // Then
        verify(versionControlSystemAdapter, times(1))
                .mapToDomain(repositoryCommitMetrics1);
        verify(versionControlSystemAdapter, times(1))
                .mapToDomain(repositoryCommitMetrics2);
    }

    @Test
    void should_save_commit_metrics_to_data_warehouse() {
        // Given
        final String organisationName = faker.animal().name();
        final Repository repository1 = Repository.builder().name(faker.animal().name()).build();
        final IRepositoryCommitMetrics repositoryCommitMetrics1 = new RepositoryCommitMetricsStub();
        final Repository repository2 = Repository.builder().name(faker.pokemon().name()).build();
        final IRepositoryCommitMetrics repositoryCommitMetrics2 = new RepositoryCommitMetricsStub();
        final VersionControlSystemAdapter versionControlSystemAdapter = Mockito.mock(VersionControlSystemAdapter.class);
        final DataWarehouseAdapter dataWarehouseAdapter = Mockito.mock(DataWarehouseAdapter.class);
        final DeliveryService deliveryService = new DeliveryService(versionControlSystemAdapter, dataWarehouseAdapter);
        when(versionControlSystemAdapter.getRepositoriesForOrganisationName(organisationName))
                .thenReturn(List.of(repository1, repository2));
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository1.getName()))
                .thenReturn(repositoryCommitMetrics1);
        when(versionControlSystemAdapter.getCommitMetricsForRepository(repository2.getName()))
                .thenReturn(repositoryCommitMetrics2);

        // When
        deliveryService.startProcessing(organisationName);

        // Then
        verify(dataWarehouseAdapter, times(1)).save(repositoryCommitMetrics1);
        verify(dataWarehouseAdapter, times(1)).save(repositoryCommitMetrics2);
    }


    private static class RepositoryCommitMetricsStub implements IRepositoryCommitMetrics {

    }
}
