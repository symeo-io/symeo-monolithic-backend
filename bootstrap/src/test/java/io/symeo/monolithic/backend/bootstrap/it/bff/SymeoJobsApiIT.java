package io.symeo.monolithic.backend.bootstrap.it.bff;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class SymeoJobsApiIT extends AbstractSymeoBackForFrontendApiIT {

    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public JobRepository jobRepository;
    @Autowired
    public JobStorage jobStorage;
    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();

    @Test
    void should_return_job_status_given_a_code() throws JsonProcessingException, SymeoException {
        // Given
        final OrganizationEntity organizationEntity = organizationRepository.save(
                OrganizationEntity.builder()
                        .id(organizationId)
                        .name(faker.rickAndMorty().character())
                        .build()
        );
        final Organization organization = OrganizationMapper.entityToDomain(organizationEntity);
        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);
        final String code1 = faker.harryPotter().character();
        final String code2 = faker.pokemon().name();

        jobStorage.createJob(
                Job.builder()
                        .code(CollectRepositoriesJobRunnable.JOB_CODE)
                        .organizationId(organization.getId())
                        .id(1L)
                        .tasks(List.of(
                                Task.builder()
                                        .input(organization)
                                        .build()
                        ))
                        .build()
        );
        final List<Job> allJobsByCodeAndOrganizationOrderByUpdateDateDesc =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(
                CollectRepositoriesJobRunnable.JOB_CODE,
                organization
        );

        System.out.println(allJobsByCodeAndOrganizationOrderByUpdateDateDesc);


//        final JobEntity jobEntity11 =
//                jobRepository.save(JobEntity.builder().organizationId(organizationId).code(code1)
//                        .technicalCreationDate(ZonedDateTime.now()).status(Job.FAILED)
//                        .tasks(tasksBytesStubs(organization))
//                        .build());
//        final JobEntity jobEntity12 =
//                jobRepository.save(JobEntity.builder().tasks(tasksBytesStubs(organization)).organizationId
//                (organizationId)
//                        .code(code1).status(Job.FINISHED).technicalCreationDate(ZonedDateTime.now()).endDate
//                        (ZonedDateTime.now()).build());
//        final JobEntity jobEntity13 =
//                jobRepository.save(JobEntity.builder().tasks(tasksBytesStubs(organization)).organizationId
//                (organizationId)
//                        .code(code1).technicalCreationDate(ZonedDateTime.now()).status(Job.STARTED).build());
//        final JobEntity jobEntity21 =
//                jobRepository.save(JobEntity.builder().tasks(tasksBytesStubs(organization)).organizationId
//                (organizationId)
//                        .code(code2).technicalCreationDate(ZonedDateTime.now()).status(Job.FAILED).build());
//        final JobEntity jobEntity22 =
//                jobRepository.save(JobEntity.builder().tasks(tasksBytesStubs(organization)).organizationId
//                (organizationId)
//                        .code(code2).technicalCreationDate(ZonedDateTime.now()).status(Job.FINISHED).build());
//        final JobEntity jobEntity23 =
//                jobRepository.save(JobEntity.builder().tasks(tasksBytesStubs(organization)).organizationId
//                (organizationId)
//                        .code(code2).technicalCreationDate(ZonedDateTime.now()).status(Job.STARTED).build());
//
//        // When
//        client.get()
//                .uri(getApiURI(JOBS_REST_API_STATUS, "job_code", code1))
//                .exchange()
//                // Then
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.errors").isEmpty()
//                .jsonPath("$.jobs.current_job.id").isEqualTo(jobEntity13.getId())
//                .jsonPath("$.jobs.current_job.status").isEqualTo(jobEntity13.getStatus())
//                .jsonPath("$.jobs.current_job.code").isEqualTo(jobEntity13.getCode())
//                .jsonPath("$.jobs.current_job.creation_date").isNotEmpty()
//                .jsonPath("$.jobs.previous_job.id").isEqualTo(jobEntity12.getId())
//                .jsonPath("$.jobs.previous_job.status").isEqualTo(jobEntity12.getStatus())
//                .jsonPath("$.jobs.previous_job.code").isEqualTo(jobEntity12.getCode())
//                .jsonPath("$.jobs.previous_job.creation_date").isNotEmpty()
//                .jsonPath("$.jobs.previous_job.end_date").isNotEmpty();
//        client.get()
//                .uri(getApiURI(JOBS_REST_API_STATUS, "job_code", code2))
//                .exchange()
//                // Then
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.errors").isEmpty()
//                .jsonPath("$.jobs.current_job.id").isEqualTo(jobEntity23.getId())
//                .jsonPath("$.jobs.current_job.status").isEqualTo(jobEntity23.getStatus())
//                .jsonPath("$.jobs.current_job.code").isEqualTo(jobEntity23.getCode())
//                .jsonPath("$.jobs.current_job.creation_date").isNotEmpty()
//                .jsonPath("$.jobs.previous_job.id").isEqualTo(jobEntity22.getId())
//                .jsonPath("$.jobs.previous_job.status").isEqualTo(jobEntity22.getStatus())
//                .jsonPath("$.jobs.previous_job.code").isEqualTo(jobEntity22.getCode())
//                .jsonPath("$.jobs.previous_job.creation_date").isNotEmpty()
//                .jsonPath("$.jobs.previous_job.end_date").isEmpty();

    }


//    private static byte[] tasksBytesStubs(final Organization organization) throws JsonProcessingException {
//        return new ObjectMapper().writeValueAsBytes(
//                List.of(
//                        Task.builder()
//                                .input(organization)
//                                .build(),
//                        Task.builder()
//                                .input(organization)
//                                .build()
//                )
//        );
//    }

    private static List<Task> tasksBytesStubs(final Organization organization) {
        return
                List.of(
                        Task.builder()
                                .input(organization)
                                .build(),
                        Task.builder()
                                .input(organization)
                                .build()

                );
    }


}
