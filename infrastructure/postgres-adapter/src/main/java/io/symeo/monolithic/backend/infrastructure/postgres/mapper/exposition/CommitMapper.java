package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import io.symeo.monolithic.backend.job.domain.model.vcs.Commit;

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
                .repositoryId(commit.getRepositoryId())
                .parentShaList(commit.getParentShaList())
                .build();
    }

    static CommitView entityToDomain(final CommitEntity commitEntity) {
        return CommitView.builder()
                .sha(commitEntity.getSha())
                .message(commitEntity.getMessage())
                .author(commitEntity.getAuthorLogin())
                .date(Date.from(commitEntity.getDate().toInstant()))
                .repositoryId(commitEntity.getRepositoryId())
                .parentShaList(commitEntity.getParentShaList())
                .build();
    }

}
