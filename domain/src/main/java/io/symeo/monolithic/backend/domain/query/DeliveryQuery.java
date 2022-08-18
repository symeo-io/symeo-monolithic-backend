package io.symeo.monolithic.backend.domain.query;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.util.List;

public class DeliveryQuery {

    private final RawStorageAdapter rawStorageAdapter;
    private final VersionControlSystemAdapter versionControlSystemAdapter;

    public DeliveryQuery(RawStorageAdapter rawStorageAdapter,
                         VersionControlSystemAdapter versionControlSystemAdapter) {
        this.rawStorageAdapter = rawStorageAdapter;
        this.versionControlSystemAdapter = versionControlSystemAdapter;
    }

    public List<Repository> readRepositoriesForOrganization(Organization organization) throws SymeoException {
        final byte[] repositoriesBytes =
                rawStorageAdapter.read(organization.getId(),
                        versionControlSystemAdapter.getName(), Repository.ALL);
        return versionControlSystemAdapter.repositoriesBytesToDomain(repositoriesBytes);
    }
}
