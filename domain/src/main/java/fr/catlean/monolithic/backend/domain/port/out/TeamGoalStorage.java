package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;

import java.util.List;
import java.util.UUID;

public interface TeamGoalStorage {

    void saveTeamGoal(TeamGoal teamGoal) throws CatleanException;

    List<TeamGoal> readForTeamId(UUID teamId) throws CatleanException;

    void deleteForId(UUID teamGoalId) throws CatleanException;

    void updateForIdAndValue(UUID id, Integer value) throws CatleanException;
}
