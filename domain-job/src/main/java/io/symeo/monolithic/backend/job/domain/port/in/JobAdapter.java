package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface JobAdapter {
    void startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                         String vcsOrganizationId) throws SymeoException;

    void startToCollectVcsDataForOrganizationIdAndRepositoryIds(UUID organizationId, List<String> repositoryIds) throws SymeoException;

    void startAll() throws SymeoException;
}
