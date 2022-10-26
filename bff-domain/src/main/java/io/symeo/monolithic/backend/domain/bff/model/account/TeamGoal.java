package io.symeo.monolithic.backend.domain.bff.model.account;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@Builder
public class TeamGoal {
    UUID id;
    @NonNull
    String standardCode;
    @NonNull
    UUID teamId;
    String value;

    public static TeamGoal fromTeamStandardAndTeamId(final TeamStandard teamStandard,
                                                     final UUID teamId,
                                                     final Integer value) {
        return TeamGoal.builder()
                .standardCode(teamStandard.code)
                .teamId(teamId)
                .value(value.toString())
                .build();
    }

    public Integer getValueAsInteger() {
        return Integer.parseInt(this.value);
    }
}
