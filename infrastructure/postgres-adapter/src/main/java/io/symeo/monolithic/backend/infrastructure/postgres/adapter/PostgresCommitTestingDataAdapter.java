package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.port.out.CommitTestingDataStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.CommitTestingDataMapper.domainToEntity;

@AllArgsConstructor
@Slf4j
public class PostgresCommitTestingDataAdapter implements CommitTestingDataStorage {
    private final CommitTestingDataRepository commitTestingDataRepository;

    @Override
    public void save(CommitTestingData commitTestingData) throws SymeoException {
        try {
            this.commitTestingDataRepository.save(domainToEntity(commitTestingData));
        } catch (Exception e) {
            final String message = "Failed to create commit testing data";
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
