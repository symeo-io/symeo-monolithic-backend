package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;

public interface TeamGoalStorage {

    void saveTeamGoal(TeamGoal teamGoal);
}
