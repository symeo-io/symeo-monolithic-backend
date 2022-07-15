package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamAccount {
    String name;
}
