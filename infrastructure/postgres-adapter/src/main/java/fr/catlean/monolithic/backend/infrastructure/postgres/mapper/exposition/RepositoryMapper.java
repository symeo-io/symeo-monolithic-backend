package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

public interface RepositoryMapper {

    static RepositoryEntity domainToEntity(final Repository repository) {
        return RepositoryEntity.builder()
                .id(isNull(
                        repository.getId()) ? UUID.randomUUID().toString() : repository.getId().toString()
                )
                .vcsId(repository.getVcsId())
                .lastUpdateDate(
                        ZonedDateTime.ofInstant(repository.getLastUpdateDate().toInstant(), ZoneId.systemDefault()
                        ))
                .creationDate(
                        ZonedDateTime.ofInstant(repository.getCreationDate().toInstant(), ZoneId.systemDefault())
                )
                .vcsOrganizationName(repository.getVcsOrganizationName())
                .organizationId(repository.getOrganization().getExternalId())
                .build();
    }
}
