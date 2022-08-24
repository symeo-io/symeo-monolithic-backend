package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface DataProcessingJobAdapter {
    void start(UUID organizationId) throws SymeoException;

    void startAll() throws SymeoException;
}
