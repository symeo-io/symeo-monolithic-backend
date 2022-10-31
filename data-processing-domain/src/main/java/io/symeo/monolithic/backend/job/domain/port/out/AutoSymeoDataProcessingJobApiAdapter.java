package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface AutoSymeoDataProcessingJobApiAdapter {

    void autoStartDataProcessingJobForOrganizationIdAndVcsOrganizationId(UUID organizationId, Long vcsOrganizationId) throws SymeoException;
}
