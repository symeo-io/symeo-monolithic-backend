package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class VcsConfiguration {
    String organizationName;
    @Builder.Default
    List<VcsTeam> vcsTeams = new ArrayList<>();

    public List<String> getAllTeamsRepositories() {
        return this.vcsTeams.stream()
                .map(VcsTeam::getVcsRepositoryNames).flatMap(Collection::stream).toList();
    }
}
