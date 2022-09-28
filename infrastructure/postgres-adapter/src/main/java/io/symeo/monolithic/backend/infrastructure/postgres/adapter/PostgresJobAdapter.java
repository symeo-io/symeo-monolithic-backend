package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper.*;

@AllArgsConstructor
@Slf4j
public class PostgresJobAdapter implements JobStorage {

    private final JobRepository jobRepository;

    @Override
    public Job createJob(Job job) throws SymeoException {
        return save(job);
    }

    @Override
    public Job updateJob(Job job) throws SymeoException {
        return save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws SymeoException {
        try {
            List<Job> jobs = new ArrayList<>();
            for (JobEntity jobEntity :
                    jobRepository.findAllByCodeAndAndOrganizationIdOrderByTechnicalModificationDate(code,
                            organization.getId())) {
                Job job = entityToDomain(jobEntity);
                jobs.add(job);
            }
            return jobs;
        } catch (Exception e) {
            LOGGER.error("Failed to read jobs for code {} and organization {}", code, organization, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to read jobs for code " + code + " and organization " + organization)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(final String code,
                                                                                                final UUID organizationId,
                                                                                                final UUID teamId,
                                                                                                final int numberOfJobsToFind) throws SymeoException {
        try {
            List<Job> jobs = new ArrayList<>();
            for (JobEntity jobEntity :
                    jobRepository.findLastJobsForCodeAndOrganizationAndLimitAndTeamByTechnicalModificationDate(code,
                            organizationId, teamId, numberOfJobsToFind)) {
                Job job = entityToDomain(jobEntity);
                jobs.add(job);
            }
            return jobs;
        } catch (Exception e) {
            final String message = String.format("Failed to read last %s jobs for code %s and organization %s and " +
                            "team %s",
                    numberOfJobsToFind, code,
                    organizationId, teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    private Job save(Job job) throws SymeoException {
        try {
            return entityToDomain(jobRepository.save(domainToEntity(job)));
        } catch (Exception e) {
            LOGGER.error("Failed to save job {}", job, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to save job " + job.getId())
                    .rootException(e)
                    .build();
        }
    }

    @Override
    public void updateJobWithTasksForJobId(Long jobId, List<Task> tasks) throws SymeoException {
        try {
            jobRepository.updateTasksForJobId(jobId, mapTasksToJsonString(tasks));
        } catch (Exception e) {
            final String message = String.format("Failed to update job %s with tasks %s", jobId, tasks);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }

    @Override
    public List<Job> findLastFailedJobsForOrganizationIdAndTeamIdForEachJobCode(UUID organizationId, UUID teamId) {
        return null;
    }
}
