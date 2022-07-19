package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;

public interface AccountTeamStorage {

    Team createTeam(Team team) throws CatleanException;
}
