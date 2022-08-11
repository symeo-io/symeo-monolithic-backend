package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.job;

import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.nonNull;

public interface JobMapper {

    static JobEntity domainToEntity(final Job job) {
        return JobEntity.builder()
                .id(nonNull(job.getId()) ? job.getId() : null)
                .code(job.getCode())
                .status(job.getStatus())
                .organizationId(job.getOrganizationId())
                .endDate(nonNull(job.getEndDate()) ? ZonedDateTime.ofInstant(job.getEndDate().toInstant(),
                        ZoneId.systemDefault()) : null)
                .build();
    }

    static Job entityToDomain(final JobEntity jobEntity) {
        return Job.builder()
                .id(jobEntity.getId())
                .code(jobEntity.getCode())
                .status(jobEntity.getStatus())
                .organizationId(jobEntity.getOrganizationId())
                .endDate(nonNull(jobEntity.getEndDate()) ? Date.from(jobEntity.getEndDate().toInstant()) : null)
                .creationDate(Date.from(jobEntity.getTechnicalCreationDate().toInstant()))
                .build();
    }
}
