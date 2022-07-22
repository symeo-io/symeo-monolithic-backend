package fr.catlean.monolithic.backend.domain.job;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

public interface JobRunnable {

    void run() throws CatleanException;
}
