package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.*;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcsOrganization {
    @NonNull
    String id;
    @NonNull
    String externalId;
    @NonNull
    String name;
    @NonNull
    String vcsId;
    @NonNull
    UUID organizationId;
}
