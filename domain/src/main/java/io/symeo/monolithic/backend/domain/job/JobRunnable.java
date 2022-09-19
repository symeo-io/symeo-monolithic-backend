package io.symeo.monolithic.backend.domain.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;

public interface JobRunnable {

    void run(Long jobIb) throws SymeoException;

    String getCode();

    List<Task> getTasks();

    void initializeTasks() throws SymeoException;
}
