package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.CommitTestingDataMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.port.out.CommitTestingDataStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Optional<CommitTestingData> getLastTestingDataForRepoAndBranchAndDate(UUID organizationId, String repoName, String branchName, Date date) throws SymeoException {
        try {
            return this.commitTestingDataRepository
                    .findByOrganizationIdAndRepositoryNameAndBranchNameAndTechnicalCreationDateBefore(organizationId, repoName, branchName, ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                    .map(CommitTestingDataMapper::entityToDomain);
        } catch (Exception e) {
            final String message = "Failed to fetch commit testing data";
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
