package io.symeo.monolithic.backend.domain.bff.model.vcs;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class RepositoryView {
    String id;
    String name;
    UUID organizationId;
    String vcsOrganizationId;
    String vcsOrganizationName;
    String defaultBranch;
}
