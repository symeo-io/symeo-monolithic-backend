package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

public interface RawStorageAdapter {
    void save(String organization, String adapterName, String contentName, byte[] bytes) throws CatleanException;

    byte[] read(String organization, String adapterName, String contentName) throws CatleanException;

    boolean exists(String organization, String adapterName, String contentName) throws CatleanException;
}
