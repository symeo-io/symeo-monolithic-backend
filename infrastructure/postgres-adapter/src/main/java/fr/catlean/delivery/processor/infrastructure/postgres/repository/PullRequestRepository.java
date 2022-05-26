package fr.catlean.delivery.processor.infrastructure.postgres.repository;

import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {
}
