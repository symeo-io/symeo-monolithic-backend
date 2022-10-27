package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;

public interface CommitTestingDataFacadeAdapter {
    void save(CommitTestingData commitTestingData);
}
