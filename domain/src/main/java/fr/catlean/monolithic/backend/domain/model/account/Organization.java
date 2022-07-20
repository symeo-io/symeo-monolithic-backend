package fr.catlean.monolithic.backend.domain.model.account;

import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import lombok.Builder;
import lombok.Value;

import java.time.ZoneId;
import java.util.*;

@Builder(toBuilder = true)
@Value
public class Organization {
    UUID id;
    String name;
    @Builder.Default
    List<Team> teams = new ArrayList<>();
    @Builder.Default
    TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
    String externalId;

    public List<String> getAllTeamsRepositories() {
        return this.teams.stream()
                .map(Team::getRepositories)
                .flatMap(Collection::stream)
                .map(Repository::getName)
                .toList();
    }
}
