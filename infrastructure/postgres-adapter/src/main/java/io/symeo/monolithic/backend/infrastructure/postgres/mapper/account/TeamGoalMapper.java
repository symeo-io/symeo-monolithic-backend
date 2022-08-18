package io.symeo.monolithic.backend.infrastructure.postgres.mapper.account;

import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface TeamGoalMapper {

    static TeamGoalEntity domainToEntity(final TeamGoal teamGoal) {
        return TeamGoalEntity
                .builder()
                .id(isNull(teamGoal.getId()) ? UUID.randomUUID() : teamGoal.getId())
                .standardCode(teamGoal.getStandardCode())
                .value(teamGoal.getValue())
                .teamId(teamGoal.getTeamId())
                .build();
    }

    static TeamGoal entityToDomain(final TeamGoalEntity teamGoalEntity) {
        return TeamGoal.builder()
                .teamId(teamGoalEntity.getTeamId())
                .standardCode(teamGoalEntity.getStandardCode())
                .value(teamGoalEntity.getValue())
                .id(teamGoalEntity.getId())
                .build();
    }
}
