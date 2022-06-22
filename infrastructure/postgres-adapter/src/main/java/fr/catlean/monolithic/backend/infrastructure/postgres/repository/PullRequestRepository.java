package fr.catlean.monolithic.backend.infrastructure.postgres.repository;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {
}
