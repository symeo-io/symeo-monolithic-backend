package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamGoalFacadeAdapter {
    void createTeamGoalForTeam(UUID teamId, String standardCode, Integer value) throws SymeoException;

    List<TeamGoal> readForTeamId(UUID teamId) throws SymeoException;

    void deleteTeamGoalForId(UUID teamGoalId) throws SymeoException;

    void updateTeamGoalForTeam(UUID id, Integer value) throws SymeoException;

    TeamGoal getTeamGoalForTeamIdAndTeamStandard(UUID teamId, TeamStandard teamStandard) throws SymeoException;

    Optional<TeamGoal> getOptionalTeamGoalForTeamIdAndTeamStandard(UUID teamId, TeamStandard teamStandard) throws SymeoException;
}
