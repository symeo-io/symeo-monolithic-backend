package io.symeo.monolithic.backend.domain.bff.model.vcs;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Builder
@Value
public class CommitView {
    String author;
    Date date;
    String message;
    String sha;
    @Builder.Default
    List<String> parentShaList = List.of();
    String repositoryId;
}
