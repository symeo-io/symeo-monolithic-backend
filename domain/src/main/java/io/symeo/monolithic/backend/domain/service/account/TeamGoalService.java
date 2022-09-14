package io.symeo.monolithic.backend.domain.service.account;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.TeamGoalStorage;
import io.symeo.monolithic.backend.domain.port.out.TeamStandardStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.AllArgsConstructor;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public
class TeamGoalService implements TeamGoalFacadeAdapter {

    private final TeamStandardStorage teamStandardStorage;
    private final TeamGoalStorage teamGoalStorage;

    @Override
    public void createTeamGoalForTeam(UUID teamId, String standardCode, Integer value) throws SymeoException {
        final TeamStandard teamStandard = teamStandardStorage.getByCode(standardCode);
        final TeamGoal teamGoal = TeamGoal.fromTeamStandardAndTeamId(teamStandard, teamId, value);
        teamGoalStorage.saveTeamGoal(teamGoal);
    }

    @Override
    public List<TeamGoal> readForTeamId(UUID teamId) throws SymeoException {
        return teamGoalStorage.readForTeamId(teamId);
    }

    @Override
    public void deleteTeamGoalForId(UUID teamGoalId) throws SymeoException {
        teamGoalStorage.deleteForId(teamGoalId);
    }

    @Override
    public void updateTeamGoalForTeam(UUID id, Integer value) throws SymeoException {
        teamGoalStorage.updateForIdAndValue(id, value);
    }

    @Override
    public TeamGoal getTeamGoalForTeamIdAndTeamStandard(final UUID teamId, final TeamStandard teamStandard) throws SymeoException {
        final List<TeamGoal> teamGoals = teamGoalStorage.readForTeamId(teamId);
        validateTeamGoals(teamGoals, teamId);
        return teamGoals
                .stream().filter(teamGoal -> teamGoal.getStandardCode().equals(teamStandard.getCode()))
                .findFirst()
                .orElseThrow(() -> SymeoException.builder()
                        .code(SymeoExceptionCode.TEAM_STANDARD_NOT_FOUND)
                        .message(String.format("Team standard not found for code %s", teamStandard.getCode()))
                        .build());
    }

    @Override
    public Optional<TeamGoal> getOptionalTeamGoalForTeamIdAndTeamStandard(final UUID teamId, final TeamStandard teamStandard) throws SymeoException {
        final List<TeamGoal> teamGoals = teamGoalStorage.readForTeamId(teamId);
        return teamGoals
                .stream().filter(teamGoal -> teamGoal.getStandardCode().equals(teamStandard.getCode()))
                .findFirst();
    }

    private static void validateTeamGoals(final List<TeamGoal> teamGoals, final UUID teamId) throws SymeoException {
        if (teamGoals.isEmpty()) {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.TEAM_NOT_FOUND)
                    .message(String.format("Team not found for id %s", teamId))
                    .build();
        }
    }
}
