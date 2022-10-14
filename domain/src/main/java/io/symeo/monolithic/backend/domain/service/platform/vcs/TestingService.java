package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@AllArgsConstructor
@Slf4j
public class TestingService {
    private final VersionControlSystemAdapter versionControlSystemAdapter;

    private final ExpositionStorageAdapter expositionStorageAdapter;

    public void collectTestingDataForOrganizationAndRepositoryFromLastCollectionDate(final Organization organization,
                                                                                 Repository repository,
                                                                                 final Date lastCollectionDate) throws SymeoException {
    }
}
