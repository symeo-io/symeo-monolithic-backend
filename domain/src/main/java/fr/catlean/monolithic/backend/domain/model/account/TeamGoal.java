package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@Builder
public class TeamGoal {

    @NonNull
    String standardCode;
    @NonNull
    UUID teamId;

    public static TeamGoal fromTeamStandardAndTeamId(TeamStandard teamStandard, UUID teamId) {
        return TeamGoal.builder().standardCode(teamStandard.code).teamId(teamId).build();
    }
}
