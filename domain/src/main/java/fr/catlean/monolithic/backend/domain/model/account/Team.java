package fr.catlean.monolithic.backend.domain.model.account;

import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Team {
    private static final String ALL = "All";

    UUID id;
    String name;
    UUID organizationId;
    List<Repository> repositories;
    @Builder.Default
    Integer pullRequestLineNumberLimit = 1000;
    @Builder.Default
    Integer pullRequestDayNumberLimit = 5;

    public static Team buildTeamAll(final UUID organizationId) {
        return Team.builder()
                .organizationId(organizationId)
                .name(ALL)
                .build();
    }
}
