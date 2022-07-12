package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

public interface DataProcessingJobAdapter {
    void start(String organisationName) throws CatleanException;
}
