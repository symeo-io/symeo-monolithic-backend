package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;

public interface CommitTestingDataStorage {
    void save(CommitTestingData commitTestingData) throws SymeoException;
}
