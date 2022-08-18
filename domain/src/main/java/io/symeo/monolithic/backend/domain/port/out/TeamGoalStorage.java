package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;

import java.util.List;
import java.util.UUID;

public interface TeamGoalStorage {

    void saveTeamGoal(TeamGoal teamGoal) throws SymeoException;

    List<TeamGoal> readForTeamId(UUID teamId) throws SymeoException;

    void deleteForId(UUID teamGoalId) throws SymeoException;

    void updateForIdAndValue(UUID id, Integer value) throws SymeoException;
}
