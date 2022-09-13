package io.symeo.monolithic.backend.domain.query;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;

import java.util.List;
import java.util.UUID;

public class DeliveryQuery {

    private final TestAdapter rawStorageAdapter;
    private final VersionControlSystemAdapter versionControlSystemAdapter;

    public DeliveryQuery(TestAdapter rawStorageAdapter,
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

    public static class TestAdapter implements RawStorageAdapter {
        @Override
        public void save(UUID organizationId, String adapterName, String contentName, byte[] bytes) throws SymeoException {

        }

        @Override
        public byte[] read(UUID organizationId, String adapterName, String contentName) throws SymeoException {
            return new byte[0];
        }

        @Override
        public boolean exists(UUID organizationId, String adapterName, String contentName) throws SymeoException {
            return false;
        }
    }
}
