package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;

public interface RepositoryMapper {

    static RepositoryEntity domainToEntity(final Repository repository) {
        return RepositoryEntity.builder()
                .id(repository.getId())
                .vcsOrganizationName(repository.getVcsOrganizationName())
                .name(repository.getName())
                .organizationId(repository.getOrganization().getId().toString())
                .build();
    }

    static Repository entityToDomain(final RepositoryEntity repositoryEntity) {
        return Repository.builder()
                .name(repositoryEntity.getName())
                .id(repositoryEntity.getId())
                .vcsOrganizationName(repositoryEntity.getVcsOrganizationName())
                .build();
    }
}
