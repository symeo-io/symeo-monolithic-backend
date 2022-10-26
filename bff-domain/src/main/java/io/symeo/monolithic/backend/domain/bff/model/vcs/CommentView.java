package io.symeo.monolithic.backend.domain.bff.model.vcs;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Builder
@Value
public class CommentView {
    String id;
    Date creationDate;
}
