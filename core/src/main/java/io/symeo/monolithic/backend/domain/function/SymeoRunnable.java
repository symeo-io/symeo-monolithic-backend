package io.symeo.monolithic.backend.domain.function;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface SymeoRunnable {

    void run() throws SymeoException;
}
