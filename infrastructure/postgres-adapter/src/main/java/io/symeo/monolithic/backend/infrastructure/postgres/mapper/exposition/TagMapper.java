package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.TagEntity;

public interface TagMapper {

    static TagEntity domainToEntity(final Tag tag) {
        return TagEntity.builder()
                .name(tag.getName())
                .sha(tag.getCommitSha())
                .repositoryId(tag.getRepository().getId())
                .build();
    }

    static Tag entityToDomain(final TagEntity tagEntity) {
        return Tag.builder()
                .commitSha(tagEntity.getSha())
                .name(tagEntity.getName())
                .repository(Repository.builder()
                        .id(tagEntity.getRepositoryEntity().getId())
                        .name(tagEntity.getRepositoryEntity().getName())
                        .build())
                .build();
    }
}
