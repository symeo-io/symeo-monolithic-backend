package fr.catlean.delivery.processor.domain.model.account;

import fr.catlean.delivery.processor.domain.domain.account.TeamAccount;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@Value
public class OrganisationAccount {
    String name;
    @NonNull
    VcsConfiguration vcsConfiguration;
    @Builder.Default
    List<TeamAccount> teamAccounts = new ArrayList<>();

    public OrganisationAccount addTeam(String teamName, List<String> teamVcsRepositoryNames) {
        final List<VcsTeam> vcsTeams = this.vcsConfiguration.getVcsTeams();
        final List<TeamAccount> teamAccounts = this.getTeamAccounts();
        vcsTeams.add(VcsTeam.builder().name(teamName).vcsRepositoryNames(teamVcsRepositoryNames).build());
        teamAccounts.add(TeamAccount.builder().name(teamName).build());
        return this.toBuilder()
                .teamAccounts(teamAccounts)
                .vcsConfiguration(
                        this.vcsConfiguration.toBuilder().vcsTeams(vcsTeams).build()
                ).build();
    }
}
