package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    public static final String ALL = "repositories";
    String id;
    String name;
    UUID organizationId;
    String vcsOrganizationId;
    String vcsOrganizationName;
    String defaultBranch;
}
