package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.TagEntity;
import io.symeo.monolithic.backend.job.domain.model.vcs.Tag;

public interface TagMapper {

    static TagEntity domainToEntity(final Tag tag) {
        return TagEntity.builder()
                .name(tag.getName())
                .sha(tag.getCommitSha())
                .repositoryId(tag.getRepositoryId())
                .vcsUrl(tag.getVcsUrl())
                .build();
    }

    static TagView entityToDomainView(final TagEntity tagEntity) {
        return TagView.builder()
                .commitSha(tagEntity.getSha())
                .name(tagEntity.getName())
                .repository(RepositoryView.builder()
                        .id(tagEntity.getRepositoryEntity().getId())
                        .name(tagEntity.getRepositoryEntity().getName())
                        .build())
                .vcsUrl(tagEntity.getVcsUrl())
                .build();
    }
    static Tag entityToDomain(final TagEntity tagEntity) {
        return Tag.builder()
                .commitSha(tagEntity.getSha())
                .name(tagEntity.getName())
                .repositoryId(tagEntity.getRepositoryId())
                .vcsUrl(tagEntity.getVcsUrl())
                .build();
    }
}
