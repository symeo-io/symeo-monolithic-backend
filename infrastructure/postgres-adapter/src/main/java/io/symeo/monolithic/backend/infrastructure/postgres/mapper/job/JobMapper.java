package io.symeo.monolithic.backend.infrastructure.postgres.mapper.job;

public interface JobMapper {
//
//    ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//
//    static JobEntity domainToEntity(final Job job) throws JsonProcessingException {
//        return JobEntity.builder()
//                .id(nonNull(job.getId()) ? job.getId() : null)
//                .code(job.getCode())
//                .status(job.getStatus())
//                .organizationId(job.getOrganizationId())
//                .teamId(job.getTeamId())
//                .error(job.getError())
//                .tasks(mapTasksToJsonString(job.getTasks()))
//                .endDate(nonNull(job.getEndDate()) ? ZonedDateTime.ofInstant(job.getEndDate().toInstant(),
//                        ZoneId.systemDefault()) : null)
//                .build();
//    }
//
//    static String mapTasksToJsonString(final List<Task> tasks) throws JsonProcessingException {
//        return OBJECT_MAPPER.writeValueAsString(tasks);
//    }
//
//    static Job entityToDomain(final JobEntity jobEntity) throws IOException {
//        return Job.builder()
//                .id(jobEntity.getId())
//                .code(jobEntity.getCode())
//                .status(jobEntity.getStatus())
//                .organizationId(jobEntity.getOrganizationId())
//                .endDate(nonNull(jobEntity.getEndDate()) ? Date.from(jobEntity.getEndDate().toInstant()) : null)
//                .creationDate(isNull(jobEntity.getTechnicalCreationDate()) ? null :
//                        Date.from(jobEntity.getTechnicalCreationDate().toInstant()))
//                .tasks(isNull(jobEntity.getTasks()) ? null : taskEntityToDomain(jobEntity))
//                .error(jobEntity.getError())
//                .teamId(jobEntity.getTeamId())
//                .build();
//    }
//
//    private static List<Task> taskEntityToDomain(final JobEntity jobEntity) throws IOException {
//        switch (jobEntity.getCode()) {
//            case CollectRepositoriesJobRunnable.JOB_CODE:
//            case InitializeOrganizationSettingsJobRunnable.JOB_CODE:
//                return mapTasksInputToOrganization(jobEntity);
//            case CollectVcsDataForOrganizationJobRunnable.JOB_CODE:
//                return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
//                        .map(task -> task.toBuilder()
//                                .input(OBJECT_MAPPER.convertValue(task.getInput(), Repository.class))
//                                .build())
//                        .toList();
//            default:
//                // TODO : handle exception
//                throw new RuntimeException();
//        }
//    }
//
//    private static List<Task> mapTasksInputToOrganization(JobEntity jobEntity) throws JsonProcessingException {
//        return Stream.of(OBJECT_MAPPER.readValue(jobEntity.getTasks(), Task[].class))
//                .map(task -> task.toBuilder()
//                        .input(OBJECT_MAPPER.convertValue(task.getInput(), Organization.class))
//                        .build())
//                .toList();
//    }
}
