package fr.catlean.monolithic.backend.domain.model.account;

import fr.catlean.monolithic.backend.domain.model.Repository;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Team {
    UUID id;
    String name;
    UUID organizationId;
    List<Repository> repositories;
}
