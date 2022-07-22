package fr.catlean.monolithic.backend.infrastructure.postgres.repository.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, String>, JpaSpecificationExecutor<TeamEntity> {

    List<TeamEntity> findAllByOrganizationId(String organizationId);
}