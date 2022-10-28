package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import io.symeo.monolithic.backend.domain.bff.port.out.SymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.mapper.SymeoDataProcessingJobApiClientMapper.*;

@AllArgsConstructor
public class SymeoDataProcessingJobApiClientAdapter implements SymeoDataProcessingJobApiAdapter {

    private final SymeoHttpClient symeoHttpClient;

    @Override
    public void startDataProcessingJobForOrganizationIdAndRepositoryIds(UUID organizationId,
                                                                        List<String> repositoryIds) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndRepositoryIds(domainToRepositoriesDTO(organizationId, repositoryIds));
    }

    @Override
    public void startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(UUID organizationId, UUID teamId,
                                                                                 List<String> repositoryIds) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(domainToTeamDTO(organizationId, teamId, repositoryIds));
    }

    @Override
    public void startDataProcessingJobForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                            Long vcsOrganizationId) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndVcsOrganizationId(domainToVcsOrganizationDTO(organizationId, vcsOrganizationId));
    }
}
