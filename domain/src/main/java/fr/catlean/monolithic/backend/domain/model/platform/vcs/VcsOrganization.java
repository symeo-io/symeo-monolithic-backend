package fr.catlean.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class VcsOrganization {
    UUID id;
    String externalId;
    String name;
}
