package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;

public interface RepositoryMapper {

    static RepositoryEntity domainToEntity(final Repository repository) {
        return RepositoryEntity.builder()
                .id(repository.getId())
                .vcsOrganizationId(repository.getVcsOrganizationId())
                .name(repository.getName())
                .organizationId(repository.getOrganizationId())
                .vcsOrganizationName(repository.getVcsOrganizationName())
                .build();
    }

    static Repository entityToDomain(final RepositoryEntity repositoryEntity) {
        return Repository.builder()
                .name(repositoryEntity.getName())
                .id(repositoryEntity.getId())
                .vcsOrganizationId(repositoryEntity.getVcsOrganizationId())
                .vcsOrganizationName(repositoryEntity.getVcsOrganizationName())
                .build();
    }
}
