package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface SymeoJobApiAdapter {

    void startJobForOrganizationId(UUID organizationId) throws SymeoException;
}
