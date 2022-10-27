package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;

public interface CommitTestingDataStorage {
    void save(CommitTestingData commitTestingData);
}
