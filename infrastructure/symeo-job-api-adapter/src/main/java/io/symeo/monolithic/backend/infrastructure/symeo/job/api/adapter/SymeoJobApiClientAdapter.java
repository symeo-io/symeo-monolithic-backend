package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SymeoJobApiClientAdapter{

    private final SymeoHttpClient symeoHttpClient;

    public void startJobForOrganizationId(UUID organizationId) throws SymeoException {
        symeoHttpClient.startJobForOrganizationId(organizationId);
    }

    public void startJobForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
        symeoHttpClient.startJobForOrganizationIdAndTeamId(organizationId, teamId);
    }
}
