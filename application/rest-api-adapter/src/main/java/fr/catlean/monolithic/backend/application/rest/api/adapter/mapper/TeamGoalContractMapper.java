package fr.catlean.monolithic.backend.application.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.frontend.contract.api.model.TeamGoalResponse;
import fr.catlean.monolithic.backend.frontend.contract.api.model.TeamGoalsResponseContract;

import java.util.List;

public interface TeamGoalContractMapper {

    static TeamGoalsResponseContract domainToContract(final List<TeamGoal> teamGoals) {
        final TeamGoalsResponseContract teamGoalsResponseContract = new TeamGoalsResponseContract();
        for (TeamGoal teamGoal : teamGoals) {
            final TeamGoalResponse teamGoalResponse = new TeamGoalResponse();
            teamGoalResponse.setTeamId(teamGoal.getTeamId());
            teamGoalResponse.setCurrentValue(2.12);
            teamGoalResponse.setId(teamGoal.getId());
            teamGoalResponse.setValue(Integer.valueOf(teamGoal.getValue()));
            teamGoalResponse.setStandardCode(teamGoal.getStandardCode());
            teamGoalsResponseContract.addTeamGoalsItem(teamGoalResponse);
        }
        return teamGoalsResponseContract;
    }

    static TeamGoalsResponseContract errorToContract(final CatleanException catleanException) {
        final TeamGoalsResponseContract teamGoalsResponseContract = new TeamGoalsResponseContract();
        teamGoalsResponseContract.setErrors(List.of(CatleanErrorContractMapper.catleanExceptionToContract(catleanException)));
        return teamGoalsResponseContract;
    }
}
