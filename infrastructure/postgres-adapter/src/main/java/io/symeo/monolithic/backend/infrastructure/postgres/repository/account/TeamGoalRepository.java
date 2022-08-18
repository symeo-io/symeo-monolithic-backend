package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TeamGoalRepository extends JpaRepository<TeamGoalEntity, UUID>,
        JpaSpecificationExecutor<TeamGoalEntity> {

    List<TeamGoalEntity> findAllByTeamId(final UUID teamId);

    @Modifying
    @Query(value = "update TeamGoalEntity tge set tge.value = :value where tge.id = :id")
    @Transactional
    void updateValueForId(@Param("id") UUID id, @Param("value") String value);

    @Transactional
    void deleteAllByTeamId(UUID teamId);
}
