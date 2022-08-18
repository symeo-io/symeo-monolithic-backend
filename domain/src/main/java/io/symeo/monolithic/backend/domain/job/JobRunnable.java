package io.symeo.monolithic.backend.domain.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface JobRunnable {

    void run() throws SymeoException;

    String getCode();
}
