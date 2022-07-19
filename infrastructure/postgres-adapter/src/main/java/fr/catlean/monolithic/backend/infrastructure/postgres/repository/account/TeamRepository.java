package fr.catlean.monolithic.backend.infrastructure.postgres.repository.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, String> {
}
