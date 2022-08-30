package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;

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

    static Comment entityToDomain(final CommentEntity commentEntity) {
        return Comment.builder()
                .id(commentEntity.getId())
                .creationDate(Date.from(commentEntity.getCreationDate().toInstant()))
                .build();
    }
}
