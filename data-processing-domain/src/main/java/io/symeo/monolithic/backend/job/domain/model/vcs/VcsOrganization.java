package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.*;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcsOrganization {
    @NonNull
    String externalId;
    @NonNull
    String name;
    @NonNull
    Long id;
    @NonNull
    String vcsId;
    @NonNull
    UUID organizationId;
}
