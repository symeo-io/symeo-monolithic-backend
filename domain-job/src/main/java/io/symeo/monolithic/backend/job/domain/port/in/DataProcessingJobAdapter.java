package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface DataProcessingJobAdapter {
    void startToCollectRepositoriesForOrganizationId(UUID organizationId) throws SymeoException;

    void startToCollectVcsDataForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException;

    void startToCollectVcsDataForOrganizationId(UUID organizationId) throws SymeoException;

    void startAll() throws SymeoException;
}
