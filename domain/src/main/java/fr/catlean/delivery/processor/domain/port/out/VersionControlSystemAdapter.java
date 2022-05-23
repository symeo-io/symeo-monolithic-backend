package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.IRepositoryCommitMetrics;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.RepositoryCommitMetrics;

import java.util.List;

public interface VersionControlSystemAdapter {

    List<Repository> getRepositoriesForOrganisationName(String organisationName);

    IRepositoryCommitMetrics getCommitMetricsForRepository(String repositoryName);

    RepositoryCommitMetrics mapToDomain(IRepositoryCommitMetrics IRepositoryCommitMetrics1);
}
