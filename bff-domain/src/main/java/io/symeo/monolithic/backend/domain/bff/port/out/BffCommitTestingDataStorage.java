package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.metric.CommitTestingDataView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface BffCommitTestingDataStorage {

    Optional<CommitTestingDataView> getLastTestingDataForRepoAndBranchAndDate(UUID organizationId, String repoName,
                                                                              String branchName, Date date) throws SymeoException;
}
