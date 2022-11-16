package io.symeo.monolithic.backend.domain.bff.service.vcs;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class RepositoryService {
    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;

    public List<RepositoryView> getRepositoriesForOrganization(Organization organization) {
        return bffExpositionStorageAdapter.readRepositoriesForOrganization(organization);
    }
}
