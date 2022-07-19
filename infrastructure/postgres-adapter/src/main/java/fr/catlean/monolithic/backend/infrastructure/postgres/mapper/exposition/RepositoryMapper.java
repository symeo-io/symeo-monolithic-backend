package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;

public interface RepositoryMapper {

    static RepositoryEntity domainToEntity(final Repository repository) {
        return RepositoryEntity.builder()
                .vcsId(repository.getVcsId())
                .vcsOrganizationName(repository.getVcsOrganizationName())
                .name(repository.getName())
                .organizationId(repository.getOrganization().getId().toString())
                .build();
    }

    static Repository entityToDomain(final RepositoryEntity repositoryEntity) {
        return Repository.builder()
                .name(repositoryEntity.getName())
                .id(repositoryEntity.getId())
                .vcsId(repositoryEntity.getVcsId())
                .vcsOrganizationName(repositoryEntity.getVcsOrganizationName())
                .build();
    }
}
