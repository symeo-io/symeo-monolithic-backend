package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;

import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface CommitMapper {


    static CommitEntity domainToEntity(final Commit commit) {
        return CommitEntity.builder()
                .sha(commit.getSha())
                .date(ZonedDateTime.ofInstant(commit.getDate().toInstant(), ZoneId.systemDefault()))
                .authorLogin(commit.getAuthor())
                .message(commit.getMessage())
                .build();
    }

    static Commit entityToDomain(final CommitEntity commitEntity) {
        return Commit.builder()
                .sha(commitEntity.getSha())
                .message(commitEntity.getMessage())
                .author(commitEntity.getAuthorLogin())
                .date(Date.from(commitEntity.getDate().toInstant()))
                .build();
    }
}
