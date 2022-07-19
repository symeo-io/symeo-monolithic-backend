package fr.catlean.monolithic.backend.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

import static java.lang.Boolean.FALSE;

@Data
@Builder(toBuilder = true)
public class Onboarding {
    UUID id;
    @Builder.Default
    Boolean hasConnectedToVcs = FALSE;
    @Builder.Default
    Boolean hasConfiguredTeam = FALSE;
}
