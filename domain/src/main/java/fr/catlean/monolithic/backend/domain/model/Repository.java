package fr.catlean.monolithic.backend.domain.model;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Repository {
    public static final String ALL = "repositories";
    UUID id;
    String name;
    String vcsOrganizationName;
    String vcsId;
    Organization organization;
}
