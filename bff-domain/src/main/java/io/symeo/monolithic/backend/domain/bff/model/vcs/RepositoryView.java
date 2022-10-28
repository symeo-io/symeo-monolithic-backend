package io.symeo.monolithic.backend.domain.bff.model.vcs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryView {
    String id;
    String name;
    UUID organizationId;
    String vcsOrganizationId;
    String vcsOrganizationName;
    String defaultBranch;
}
