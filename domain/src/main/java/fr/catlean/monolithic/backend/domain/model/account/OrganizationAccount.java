package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Builder(toBuilder = true)
@Value
public class OrganizationAccount {
    String name;
    @NonNull
    VcsConfiguration vcsConfiguration;
    @Builder.Default
    List<TeamAccount> teamAccounts = new ArrayList<>();
    @Builder.Default
    TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());

    public OrganizationAccount addTeam(final String teamName, final List<String> teamVcsRepositoryNames,
                                       final Integer pullRequestLineNumberLimit,
                                       final Integer pullRequestDayNumberLimit) {
        final List<VcsTeam> vcsTeams = this.vcsConfiguration.getVcsTeams();
        final List<TeamAccount> teamAccounts = this.getTeamAccounts();
        vcsTeams.add(
                VcsTeam.builder()
                        .name(teamName)
                        .vcsRepositoryNames(teamVcsRepositoryNames)
                        .pullRequestLineNumberLimit(pullRequestLineNumberLimit)
                        .pullRequestDayNumberLimit(pullRequestDayNumberLimit)
                        .build());
        teamAccounts.add(TeamAccount.builder().name(teamName).build());
        return this.toBuilder()
                .teamAccounts(teamAccounts)
                .vcsConfiguration(
                        this.vcsConfiguration.toBuilder().vcsTeams(vcsTeams).build()
                ).build();
    }
}
