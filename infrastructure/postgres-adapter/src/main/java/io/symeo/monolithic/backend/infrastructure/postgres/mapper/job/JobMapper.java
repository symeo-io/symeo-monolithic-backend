package io.symeo.monolithic.backend.infrastructure.postgres.mapper.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .tasks(isNull(jobEntity.getTasks()) ? null : taskEntityToDomain(jobEntity))
                .build();
    }

    private static List<Task> taskEntityToDomain(final JobEntity jobEntity) throws IOException {
        if (jobEntity.getCode().equals(CollectRepositoriesJobRunnable.JOB_CODE)) {
            return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
                    .map(task -> task.toBuilder()
                            .input(OBJECT_MAPPER.convertValue(task.getInput(), Organization.class))
                            .build())
                    .collect(Collectors.toList());
        } else {
            // TODO : handle exception
            throw new RuntimeException();
        }
    }
}
