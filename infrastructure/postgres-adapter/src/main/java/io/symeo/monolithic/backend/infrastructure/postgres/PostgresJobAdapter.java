package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.job.JobMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;

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
            return jobRepository.findAllByCodeAndAndOrganizationIdOrderByTechnicalModificationDate(code,
                            organization.getId())
                    .stream()
                    .map(JobMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to read jobs for code {} and organization {}", code, organization, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to read jobs for code " + code + " and organization " + organization)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(String code, Organization organization,
                                                                                     int numberOfJobsToFind) throws SymeoException {
        try {
            return jobRepository.findLastJobsForCodeAndOrganizationAndLimitByTechnicalModificationDate(code,
                            organization.getId(), numberOfJobsToFind)
                    .stream()
                    .map(JobMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read last %s jobs for code %s and organization %s",
                    numberOfJobsToFind, code,
                    organization);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    private Job save(Job job) throws SymeoException {
        try {
            return JobMapper.entityToDomain(jobRepository.save(JobMapper.domainToEntity(job)));
        } catch (Exception e) {
            LOGGER.error("Failed to save job {}", job, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to save job " + job.getId())
                    .build();
        }
    }
}
