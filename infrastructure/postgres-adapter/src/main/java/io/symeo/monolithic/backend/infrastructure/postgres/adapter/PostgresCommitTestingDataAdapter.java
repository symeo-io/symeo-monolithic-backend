package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.port.out.CommitTestingDataStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class PostgresCommitTestingDataAdapter implements CommitTestingDataStorage {
    private final CommitTestingDataRepository commitTestingDataRepository;

    @Override
    public void save(CommitTestingData commitTestingData) {
        this.commitTestingDataRepository.save(commitTestingData);
    }
}
