package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import io.symeo.monolithic.backend.domain.port.out.SymeoJobApiAdapter;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SymeoJobApiClientAdapter implements SymeoJobApiAdapter {

    private final SymeoHttpClient symeoHttpClient;

    @Override
    public void startJobForOrganizationId(UUID organizationId) {
        symeoHttpClient.startJobForOrganizationId(organizationId);
    }
}
