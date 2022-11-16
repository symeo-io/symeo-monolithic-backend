package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;

public interface RepositoryMapper {

    static RepositoryEntity domainToEntity(final Repository repository) {
        return RepositoryEntity.builder()
                .id(repository.getId())
                .vcsOrganizationId(repository.getVcsOrganizationId())
                .name(repository.getName())
                .organizationId(repository.getOrganizationId())
                .vcsOrganizationName(repository.getVcsOrganizationName())
                .defaultBranch(repository.getDefaultBranch())
                .build();
    }

    static RepositoryView entityToDomain(final RepositoryEntity repositoryEntity) {
        return RepositoryView.builder()
                .name(repositoryEntity.getName())
                .id(repositoryEntity.getId())
                .vcsOrganizationId(repositoryEntity.getVcsOrganizationId())
                .vcsOrganizationName(repositoryEntity.getVcsOrganizationName())
                .defaultBranch(repositoryEntity.getDefaultBranch())
                .build();
    }

    static Repository entityToDataProcessingDomain(final RepositoryEntity repositoryEntity) {
        return Repository.builder()
                .name(repositoryEntity.getName())
                .id(repositoryEntity.getId())
                .vcsOrganizationId(repositoryEntity.getVcsOrganizationId())
                .vcsOrganizationName(repositoryEntity.getVcsOrganizationName())
                .defaultBranch(repositoryEntity.getDefaultBranch())
                .organizationId(repositoryEntity.getOrganizationId())
                .build();
    }
}
