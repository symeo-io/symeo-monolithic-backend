package fr.catlean.delivery.processor.infrastructure.postgres.repository;

import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestHistogramDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestHistogramRepository extends JpaRepository<PullRequestHistogramDataEntity, Long> {
}
