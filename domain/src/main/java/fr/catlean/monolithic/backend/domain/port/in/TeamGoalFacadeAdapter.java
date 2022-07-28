package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;

import java.util.List;
import java.util.UUID;

public interface TeamGoalFacadeAdapter {
    void createTeamGoalForTeam(UUID teamId, String standardCode, Integer value) throws CatleanException;

    List<TeamGoal> readForTeamId(UUID teamId) throws CatleanException;

    void deleteTeamGoalForId(UUID teamGoalId) throws CatleanException;
}
