package fr.catlean.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VcsOrganization {
    String id;
    String externalId;
    String name;
    String vcsId;
}
