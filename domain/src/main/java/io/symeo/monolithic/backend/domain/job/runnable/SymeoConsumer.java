package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface SymeoConsumer<T> {
    void accept(T t) throws SymeoException;
}
