package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.IRepositoryCommitMetrics;

public interface DataWarehouseAdapter {
    void save(IRepositoryCommitMetrics repositoryCommitMetrics);

}
