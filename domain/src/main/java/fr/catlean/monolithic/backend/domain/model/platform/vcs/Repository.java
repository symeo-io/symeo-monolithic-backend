package fr.catlean.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Repository {
    public static final String ALL = "repositories";
    String id;
    String name;
    UUID organizationId;
    String vcsOrganizationId;
    String vcsOrganizationName;
}
