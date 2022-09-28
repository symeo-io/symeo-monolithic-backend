package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;

import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface CommitMapper {


    static CommitEntity domainToEntity(final Commit commit) {
        return CommitEntity.builder()
                .sha(commit.getSha())
                .date(ZonedDateTime.ofInstant(commit.getDate().toInstant(), ZoneId.systemDefault()))
                .authorLogin(commit.getAuthor())
                .message(commit.getMessage())
                .repositoryId(commit.getRepositoryId())
                .parentShaList(commit.getParentShaList())
                .build();
    }

    static Commit entityToDomain(final CommitEntity commitEntity) {
        return Commit.builder()
                .sha(commitEntity.getSha())
                .message(commitEntity.getMessage())
                .author(commitEntity.getAuthorLogin())
                .date(Date.from(commitEntity.getDate().toInstant()))
                .repositoryId(commitEntity.getRepositoryId())
                .parentShaList(commitEntity.getParentShaList())
                .build();
    }

}
