package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface OrganizationJobFacade {
    void startAll() throws SymeoException;
}
