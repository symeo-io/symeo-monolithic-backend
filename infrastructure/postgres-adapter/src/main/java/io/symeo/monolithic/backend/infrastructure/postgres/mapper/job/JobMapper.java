package io.symeo.monolithic.backend.infrastructure.postgres.mapper.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.bff.model.job.RepositoryDateRangeTaskView;
import io.symeo.monolithic.backend.domain.bff.model.job.TaskView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectVcsDataForRepositoriesAndDatesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface JobMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static JobEntity domainToEntity(final Job job) throws SymeoException {
        return JobEntity.builder()
                .id(nonNull(job.getId()) ? job.getId() : null)
                .code(job.getCode())
                .status(job.getStatus())
                .organizationId(job.getOrganizationId())
                .error(job.getError())
                .tasks(mapTasksToJsonString(job.getTasks()))
                .endDate(nonNull(job.getEndDate()) ? ZonedDateTime.ofInstant(job.getEndDate().toInstant(),
                        ZoneId.systemDefault()) : null)
                .build();
    }

    static String mapTasksToJsonString(final List<Task> tasks) throws SymeoException {
        try {
            return OBJECT_MAPPER.writeValueAsString(tasks);
        } catch (JsonProcessingException e) {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.POSTGRES_JSON_MAPPING_ERROR)
                    .message(String.format("Failed to serialize tasks %s to json", tasks))
                    .rootException(e)
                    .build();
        }
    }

    static Job entityToDataProcessingDomain(final JobEntity jobEntity) throws SymeoException {
        return Job.builder()
                .id(jobEntity.getId())
                .code(jobEntity.getCode())
                .status(jobEntity.getStatus())
                .organizationId(jobEntity.getOrganizationId())
                .endDate(nonNull(jobEntity.getEndDate()) ? Date.from(jobEntity.getEndDate().toInstant()) : null)
                .creationDate(isNull(jobEntity.getTechnicalCreationDate()) ? null :
                        Date.from(jobEntity.getTechnicalCreationDate().toInstant()))
                .tasks(isNull(jobEntity.getTasks()) ? null : taskEntityToDataProcessingDomain(jobEntity))
                .error(jobEntity.getError())
                .build();
    }

    static JobView entityToBffDomain(final JobEntity jobEntity) throws SymeoException {
        return JobView.builder()
                .id(jobEntity.getId())
                .code(jobEntity.getCode())
                .status(jobEntity.getStatus())
                .organizationId(jobEntity.getOrganizationId())
                .endDate(nonNull(jobEntity.getEndDate()) ? Date.from(jobEntity.getEndDate().toInstant()) : null)
                .creationDate(isNull(jobEntity.getTechnicalCreationDate()) ? null :
                        Date.from(jobEntity.getTechnicalCreationDate().toInstant()))
                .tasks(isNull(jobEntity.getTasks()) ? null : taskEntityToBffDomain(jobEntity))
                .build();
    }


    private static List<Task> taskEntityToDataProcessingDomain(final JobEntity jobEntity) throws SymeoException {
        switch (jobEntity.getCode()) {
            case CollectRepositoriesJobRunnable.JOB_CODE:
                return mapTasksInputToDataProcessingVcsOrganization(jobEntity);
            case CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE:
                return mapTasksInputToDataProcessingRepositoryDateRangeTaskView(jobEntity);
            default:
                throw SymeoException.builder()
                        .code(SymeoExceptionCode.INVALID_JOB_CODE)
                        .message(String.format("Invalid job code %s", jobEntity.getCode()))
                        .build();
        }
    }

    private static List<TaskView> taskEntityToBffDomain(final JobEntity jobEntity) throws SymeoException {
        switch (jobEntity.getCode()) {
            case CollectRepositoriesJobRunnable.JOB_CODE:
                return mapTasksInputToBffVcsOrganization(jobEntity);
            case CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE:
                return mapTasksInputToBffRepositoryDateRangeTaskView(jobEntity);
            default:
                throw SymeoException.builder()
                        .code(SymeoExceptionCode.INVALID_JOB_CODE)
                        .message(String.format("Invalid job code %s", jobEntity.getCode()))
                        .build();
        }
    }


    private static List<Task> mapTasksInputToDataProcessingRepositoryDateRangeTaskView(JobEntity jobEntity) throws SymeoException {
        try {
            return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
                    .map(task -> task.toBuilder()
                            .input(OBJECT_MAPPER.convertValue(task.getInput(), RepositoryDateRangeTaskView.class))
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.POSTGRES_JSON_MAPPING_ERROR)
                    .message(String.format("Failed to deserialized json %s to Task", jobEntity))
                    .rootException(e)
                    .build();
        }
    }

    private static List<TaskView> mapTasksInputToBffRepositoryDateRangeTaskView(JobEntity jobEntity) throws SymeoException {
        try {
            return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
                    .map(task -> task.toBuilder()
                            .input(OBJECT_MAPPER.convertValue(task.getInput(), RepositoryDateRangeTaskView.class))
                            .build())
                    .map(task ->
                            TaskView.builder()
                                    .input(task.getInput())
                                    .status(task.getStatus())
                                    .build()
                    )
                    .toList();
        } catch (JsonProcessingException e) {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.POSTGRES_JSON_MAPPING_ERROR)
                    .message(String.format("Failed to deserialized json %s to Task", jobEntity))
                    .rootException(e)
                    .build();
        }
    }


    private static List<Task> mapTasksInputToDataProcessingVcsOrganization(JobEntity jobEntity) throws SymeoException {
        try {
            return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
                    .map(task -> task.toBuilder()
                            .input(OBJECT_MAPPER.convertValue(task.getInput(), VcsOrganization.class))
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.POSTGRES_JSON_MAPPING_ERROR)
                    .message(String.format("Failed to deserialized json %s to Task", jobEntity))
                    .rootException(e)
                    .build();
        }
    }

    private static List<TaskView> mapTasksInputToBffVcsOrganization(JobEntity jobEntity) throws SymeoException {
        try {
            return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
                    .map(task -> task.toBuilder()
                            .input(OBJECT_MAPPER.convertValue(task.getInput(), VcsOrganization.class))
                            .build())
                    .map(task ->
                            TaskView.builder()
                                    .input(task.getInput())
                                    .status(task.getStatus())
                                    .build()
                    )
                    .toList();
        } catch (JsonProcessingException e) {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.POSTGRES_JSON_MAPPING_ERROR)
                    .message(String.format("Failed to deserialized json %s to Task", jobEntity))
                    .rootException(e)
                    .build();
        }
    }


}
