package io.symeo.monolithic.backend.infrastructure.postgres.mapper.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface JobMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static JobEntity domainToEntity(final Job job) throws JsonProcessingException {
        return JobEntity.builder()
                .id(nonNull(job.getId()) ? job.getId() : null)
                .code(job.getCode())
                .status(job.getStatus())
                .organizationId(job.getOrganizationId())
                .tasks(OBJECT_MAPPER.writeValueAsBytes(job.getTasks()))
                .endDate(nonNull(job.getEndDate()) ? ZonedDateTime.ofInstant(job.getEndDate().toInstant(),
                        ZoneId.systemDefault()) : null)
                .build();
    }

    static Job entityToDomain(final JobEntity jobEntity) throws IOException {
        return Job.builder()
                .id(jobEntity.getId())
                .code(jobEntity.getCode())
                .status(jobEntity.getStatus())
                .organizationId(jobEntity.getOrganizationId())
                .endDate(nonNull(jobEntity.getEndDate()) ? Date.from(jobEntity.getEndDate().toInstant()) : null)
                .creationDate(isNull(jobEntity.getTechnicalCreationDate()) ? null :
                        Date.from(jobEntity.getTechnicalCreationDate().toInstant()))
                .tasks(isNull(jobEntity.getTasks()) ? null : List.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(),
                        Task[].class)))
                .build();
    }
}
