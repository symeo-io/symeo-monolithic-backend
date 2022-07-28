package fr.catlean.monolithic.backend.domain.service.account;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.TeamGoalStorage;
import fr.catlean.monolithic.backend.domain.port.out.TeamStandardStorage;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.model.account.TeamGoal.fromTeamStandardAndTeamId;

@AllArgsConstructor
public
class TeamGoalService implements TeamGoalFacadeAdapter {

    private final TeamStandardStorage teamStandardStorage;
    private final TeamGoalStorage teamGoalStorage;

    @Override
    public void createTeamGoalForTeam(UUID teamId, String standardCode, Integer value) throws CatleanException {
        final TeamStandard teamStandard = teamStandardStorage.getByCode(standardCode);
        final TeamGoal teamGoal = fromTeamStandardAndTeamId(teamStandard, teamId, value);
        teamGoalStorage.saveTeamGoal(teamGoal);
    }

    @Override
    public List<TeamGoal> readForTeamId(UUID teamId) throws CatleanException {
        return teamGoalStorage.readForTeamId(teamId);
    }

    @Override
    public void deleteTeamGoalForId(UUID teamGoalId) throws CatleanException {
        teamGoalStorage.deleteForId(teamGoalId);
    }

    @Override
    public void updateTeamGoalForTeam(UUID id, Integer value) throws CatleanException {
        teamGoalStorage.updateForIdAndValue(id, value);
    }
}
