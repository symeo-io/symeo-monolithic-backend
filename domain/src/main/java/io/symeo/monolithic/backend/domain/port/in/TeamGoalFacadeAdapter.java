package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;

import java.util.List;
import java.util.UUID;

public interface TeamGoalFacadeAdapter {
    void createTeamGoalForTeam(UUID teamId, String standardCode, Integer value) throws SymeoException;

    List<TeamGoal> readForTeamId(UUID teamId) throws SymeoException;

    void deleteTeamGoalForId(UUID teamGoalId) throws SymeoException;

    void updateTeamGoalForTeam(UUID id, Integer value) throws SymeoException;

    TeamGoal getTeamGoalForTeamIdAndTeamStandard(UUID teamId, TeamStandard teamStandard) throws SymeoException;
}
