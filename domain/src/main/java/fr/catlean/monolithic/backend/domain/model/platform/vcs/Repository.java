package fr.catlean.monolithic.backend.domain.model.platform.vcs;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Repository {
    public static final String ALL = "repositories";
    String id;
    String name;
    String vcsOrganizationName;
    Organization organization;
}
