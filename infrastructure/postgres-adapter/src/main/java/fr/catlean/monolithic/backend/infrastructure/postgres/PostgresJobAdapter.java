package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.port.out.JobStorage;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper.entityToDomain;

@AllArgsConstructor
@Slf4j
public class PostgresJobAdapter implements JobStorage {

    private final JobRepository jobRepository;

    @Override
    public Job createJob(Job job) throws CatleanException {
        return save(job);
    }

    @Override
    public Job updateJob(Job job) throws CatleanException {
        return save(job);
    }

    private Job save(Job job) throws CatleanException {
        try {
            return entityToDomain(jobRepository.save(domainToEntity(job)));
        } catch (Exception e) {
            LOGGER.error("Failed to save job {}", job, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to save job " + job.getId())
                    .build();
        }
    }
}
