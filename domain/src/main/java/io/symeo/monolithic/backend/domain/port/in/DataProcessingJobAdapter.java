package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface DataProcessingJobAdapter {
    void start(String vcsOrganizationName) throws SymeoException;
}
