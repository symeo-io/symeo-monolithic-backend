package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TeamStandard {

    public static final String TIME_TO_MERGE = "time-to-merge";
    String code;
}
