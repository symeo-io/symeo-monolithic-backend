package io.symeo.monolithic.backend.infrastructure.postgres.mapper.job;

import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.isNull;
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
                .creationDate(isNull(jobEntity.getTechnicalCreationDate()) ? null :
                        Date.from(jobEntity.getTechnicalCreationDate().toInstant()))
                .build();
    }
}