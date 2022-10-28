package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface CommitTestingDataStorage {
    void save(CommitTestingData commitTestingData) throws SymeoException;
    Optional<CommitTestingData> getLastTestingDataForRepoAndBranchAndDate(UUID organizationId, String repoName, String branchName, Date date) throws SymeoException;
}
