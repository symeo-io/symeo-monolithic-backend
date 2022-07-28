package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;

public interface TeamStandardStorage {
    TeamStandard getByCode(String standardCode);
}
