package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.port.out.SymeoJobApiAdapter;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SymeoJobApiClientAdapter implements SymeoJobApiAdapter {

    private final SymeoHttpClient symeoHttpClient;

    @Override
    public void startJobForOrganizationId(UUID organizationId) throws SymeoException {
        symeoHttpClient.startJobForOrganizationId(organizationId);
    }

    @Override
    public void startJobForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
        symeoHttpClient.startJobForOrganizationIdAndTeamId(organizationId, teamId);
    }
}
