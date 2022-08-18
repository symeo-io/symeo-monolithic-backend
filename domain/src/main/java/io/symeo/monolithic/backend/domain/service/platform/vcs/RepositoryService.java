package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class RepositoryService {
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public void saveRepositories(List<Repository> repositories) {
        expositionStorageAdapter.saveRepositories(repositories);
    }

    public List<Repository> getRepositoriesForOrganization(Organization organization) {
        return expositionStorageAdapter.readRepositoriesForOrganization(organization);
    }
}
