package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommentView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;
import io.symeo.monolithic.backend.job.domain.model.vcs.Comment;

import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface CommentMapper {

    static CommentEntity domainToEntity(final Comment comment) {
        return CommentEntity.builder()
                .creationDate(ZonedDateTime.ofInstant(comment.getCreationDate().toInstant(), ZoneId.systemDefault()))
                .id(comment.getId())
                .build();
    }

    static CommentView entityToDomain(final CommentEntity commentEntity) {
        return CommentView.builder()
                .id(commentEntity.getId())
                .creationDate(Date.from(commentEntity.getCreationDate().toInstant()))
                .build();
    }
}
