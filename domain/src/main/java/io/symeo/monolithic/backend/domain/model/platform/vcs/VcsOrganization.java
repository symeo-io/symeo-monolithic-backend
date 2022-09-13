package io.symeo.monolithic.backend.domain.model.platform.vcs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcsOrganization {
    String id;
    String externalId;
    String name;
    String vcsId;
}
