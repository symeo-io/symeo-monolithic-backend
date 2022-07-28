package fr.catlean.monolithic.backend.infrastructure.postgres.repository.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TeamGoalRepository extends JpaRepository<TeamGoalEntity, UUID>,
        JpaSpecificationExecutor<TeamGoalEntity> {

    List<TeamGoalEntity> findAllByTeamId(final UUID teamId);
}
