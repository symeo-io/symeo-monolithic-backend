package io.symeo.monolithic.backend.job.domain.model.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.function.Consumer;

public interface SymeoConsumer<T> {
    void accept(T t) throws SymeoException;
}
