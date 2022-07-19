package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
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
