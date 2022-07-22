package fr.catlean.monolithic.backend.infrastructure.postgres.repository.job;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
}
