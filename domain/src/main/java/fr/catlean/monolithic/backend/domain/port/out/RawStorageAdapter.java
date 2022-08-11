package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

import java.util.UUID;

public interface RawStorageAdapter {
    void save(UUID organizationId, String adapterName, String contentName, byte[] bytes) throws CatleanException;

    byte[] read(UUID organizationId, String adapterName, String contentName) throws CatleanException;

    boolean exists(UUID organizationId, String adapterName, String contentName) throws CatleanException;
}
