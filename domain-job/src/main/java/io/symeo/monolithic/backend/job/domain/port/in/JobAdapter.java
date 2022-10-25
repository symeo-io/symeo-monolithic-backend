package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface JobAdapter {
    void startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                         String vcsOrganizationId) throws SymeoException;

    void startToCollectVcsDataForOrganizationIdAndVcsOrganizationId(UUID organizationId) throws SymeoException;

    void startAll() throws SymeoException;
}
