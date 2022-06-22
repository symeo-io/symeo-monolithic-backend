package fr.catlean.monolithic.backend.infrastructure.postgres.repository;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.PullRequestHistogramDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestHistogramRepository extends JpaRepository<PullRequestHistogramDataEntity, Long> {
}
