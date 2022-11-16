package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.*;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    public static final String ALL = "repositories";
    @NonNull
    String id;
    @NonNull
    String name;
    @NonNull
    UUID organizationId;
    @NonNull
    String vcsOrganizationId;
    @NonNull
    String vcsOrganizationName;
    String defaultBranch;
}
