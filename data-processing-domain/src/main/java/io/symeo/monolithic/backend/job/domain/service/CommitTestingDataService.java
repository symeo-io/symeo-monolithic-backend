package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.CommitTestingDataStorage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommitTestingDataService implements CommitTestingDataFacadeAdapter {
    private final CommitTestingDataStorage commitTestingDataStorage;

    @Override
    public void save(CommitTestingData commitTestingData) throws SymeoException {
        this.commitTestingDataStorage.save(commitTestingData);
    }
}
