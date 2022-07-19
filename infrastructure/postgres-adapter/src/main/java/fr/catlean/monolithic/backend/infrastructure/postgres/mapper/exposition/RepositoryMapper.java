package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface RepositoryMapper {

    static RepositoryEntity domainToEntity(final Repository repository) {
        return RepositoryEntity.builder()
                .id(repository.getVcsId())
                .vcsOrganizationName(repository.getVcsOrganizationName())
                .name(repository.getName())
                .organizationId(repository.getOrganization().getId().toString())
                .build();
    }
}
