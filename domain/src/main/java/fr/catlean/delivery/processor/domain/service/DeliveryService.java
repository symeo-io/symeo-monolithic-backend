package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.model.IRepositoryCommitMetrics;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.RepositoryCommitMetrics;
import fr.catlean.delivery.processor.domain.port.out.DataWarehouseAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DeliveryService {
    private VersionControlSystemAdapter versionControlSystemAdapter;
    private DataWarehouseAdapter dataWarehouseAdapter;

    public void startProcessing(String organisationName) {
        processCommitMetricsByWeeks(organisationName);

    }

    private void processCommitMetricsByWeeks(String organisationName) {
        final List<Repository> repositories = this.versionControlSystemAdapter.getRepositoriesForOrganisationName(organisationName);
        for (Repository repository : repositories) {
            final IRepositoryCommitMetrics commitMetricsFromVcsAdapter = this.versionControlSystemAdapter.getCommitMetricsForRepository(repository.getName());
            this.dataWarehouseAdapter.save(commitMetricsFromVcsAdapter);
            final RepositoryCommitMetrics repositoryCommitMetrics = this.versionControlSystemAdapter.mapToDomain(commitMetricsFromVcsAdapter);
        }
    }
}
