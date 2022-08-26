package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface CommitMapper {


    static CommitEntity domainToEntity(final Commit commit, final String pullRequestId) {
        return CommitEntity.builder()
                .pullRequestId(pullRequestId)
                .sha(commit.getSha())
                .date(ZonedDateTime.ofInstant(commit.getDate().toInstant(), ZoneId.systemDefault()))
                .authorLogin(commit.getAuthor())
                .message(commit.getMessage())
                .build();
    }
}
