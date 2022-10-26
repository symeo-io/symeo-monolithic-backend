package io.symeo.monolithic.backend.domain.bff.model.vcs;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TagView {
    String name;
    String commitSha;
    RepositoryView repository;
    String vcsUrl;
}
