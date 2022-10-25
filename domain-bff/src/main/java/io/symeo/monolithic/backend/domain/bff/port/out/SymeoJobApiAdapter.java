package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface SymeoJobApiAdapter {

    void startJobForOrganizationId(UUID organizationId) throws SymeoException;

    void startJobForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException;

}