package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.frontend.contract.api.model.TeamGoalResponse;
import io.symeo.monolithic.backend.frontend.contract.api.model.TeamGoalsResponseContract;

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

    static TeamGoalsResponseContract errorToContract(final SymeoException symeoException) {
        final TeamGoalsResponseContract teamGoalsResponseContract = new TeamGoalsResponseContract();
        teamGoalsResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return teamGoalsResponseContract;
    }
}
