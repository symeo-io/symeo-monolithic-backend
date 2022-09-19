package io.symeo.monolithic.backend.domain.model.account;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.util.*;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    UUID id;
    String name;
    @Builder.Default
    List<Team> teams = new ArrayList<>();
    @Builder.Default
    TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
    VcsOrganization vcsOrganization;

    public List<String> getAllTeamsRepositories() {
        return this.teams.stream()
                .map(Team::getRepositories)
                .flatMap(Collection::stream)
                .map(Repository::getName)
                .toList();
    }

}
