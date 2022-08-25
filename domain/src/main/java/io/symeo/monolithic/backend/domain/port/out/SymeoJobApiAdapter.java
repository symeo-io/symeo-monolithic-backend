package io.symeo.monolithic.backend.domain.port.out;

import java.util.UUID;

public interface SymeoJobApiAdapter {

    void startJobForOrganizationId(UUID organizationId);
}
