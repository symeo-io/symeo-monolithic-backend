package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.UUID;

public interface RawStorageAdapter {
    void save(UUID organizationId, String adapterName, String contentName, byte[] bytes) throws SymeoException;

    byte[] read(UUID organizationId, String adapterName, String contentName) throws SymeoException;

    boolean exists(UUID organizationId, String adapterName, String contentName) throws SymeoException;
}
