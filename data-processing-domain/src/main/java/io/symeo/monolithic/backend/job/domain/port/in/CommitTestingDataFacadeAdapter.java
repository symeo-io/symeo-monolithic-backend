package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommitTestingDataFacadeAdapter {
    void save(CommitTestingData commitTestingData) throws SymeoException;

    Optional<CommitTestingData> getLastTestingDataForRepoAndBranchAndDate(UUID organizationId, String repoName, String branchName, Date date) throws SymeoException;
    Boolean hasDataForOrganizationAndRepositories(UUID organizationId, List<String> repoNames) throws SymeoException;
}
