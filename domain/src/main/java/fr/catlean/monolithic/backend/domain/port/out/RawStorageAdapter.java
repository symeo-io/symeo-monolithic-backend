package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

public interface RawStorageAdapter {
    void save(String organizationId, String adapterName, String contentName, byte[] bytes) throws CatleanException;

    byte[] read(String organizationId, String adapterName, String contentName) throws CatleanException;

    boolean exists(String organizationId, String adapterName, String contentName) throws CatleanException;
}
