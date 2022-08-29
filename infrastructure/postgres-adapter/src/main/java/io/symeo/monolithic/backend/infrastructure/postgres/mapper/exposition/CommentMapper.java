package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface CommentMapper {

    static CommentEntity domainToEntity(final Comment comment, final PullRequest pullRequest) {
        return CommentEntity.builder()
                .creationDate(ZonedDateTime.ofInstant(comment.getCreationDate().toInstant(), ZoneId.systemDefault()))
                .id(comment.getId())
                .pullRequestId(pullRequest.getId())
                .build();
    }
}
