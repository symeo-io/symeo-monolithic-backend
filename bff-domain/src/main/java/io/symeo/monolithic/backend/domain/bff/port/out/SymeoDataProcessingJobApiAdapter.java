package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface SymeoDataProcessingJobApiAdapter {

    void startDataProcessingJobForOrganizationIdAndRepositoryIds(UUID organizationId, List<String> repositoryIds) throws SymeoException;

    void startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(UUID organizationId, UUID teamId,
                                                                          List<String> repositoryIds) throws SymeoException;

    void startDataProcessingJobForOrganizationIdAndVcsOrganizationId(UUID organizationId, Long vcsOrganizationId) throws SymeoException;

}
