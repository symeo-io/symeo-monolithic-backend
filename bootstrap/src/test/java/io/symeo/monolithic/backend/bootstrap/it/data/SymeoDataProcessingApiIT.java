package io.symeo.monolithic.backend.bootstrap.it.data;

import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.bff.contract.api.model.DeployDetectionSettingsContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static java.time.ZonedDateTime.ofInstant;
import static org.assertj.core.api.Assertions.assertThat;

public class SymeoDataProcessingApiIT extends AbstractSymeoDataCollectionAndApiIT {

    @Autowired
    VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;
    @Autowired
    RepositoryRepository repositoryRepository;
    @Autowired
    PullRequestRepository pullRequestRepository;
    @Autowired
    CommitRepository commitRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    CycleTimeRepository cycleTimeRepository;
    @Autowired
    JobRepository jobRepository;
    private static final Faker faker = new Faker();

    @Test
    @Order(1)
    void should_start_data_collection_jobs_for_all_organization_ids() {
        // Given
        final UUID organizationId1 = UUID.randomUUID();
        final UUID organizationId2 = UUID.randomUUID();
        final UUID organizationId3 = UUID.randomUUID();
        final List<VcsOrganizationEntity> vcsOrganizationEntities = vcsOrganizationRepository.saveAll(List.of(
                VcsOrganizationEntity.builder()
                        .organizationEntity(OrganizationEntity.builder().id(organizationId1).name(FAKER.rickAndMorty().character()).build())
                        .name(FAKER.name().firstName())
                        .vcsId(FAKER.pokemon().location())
                        .externalId(FAKER.ancient().god())
                        .build(),
                VcsOrganizationEntity.builder()
                        .organizationEntity(OrganizationEntity.builder().id(organizationId2).name(FAKER.rickAndMorty().character()).build())
                        .name(FAKER.name().firstName())
                        .vcsId(FAKER.pokemon().location())
                        .externalId(FAKER.ancient().god())
                        .build(),
                VcsOrganizationEntity.builder()
                        .organizationEntity(OrganizationEntity.builder().id(organizationId3).name(FAKER.rickAndMorty().character()).build())
                        .name(FAKER.name().firstName())
                        .vcsId(FAKER.pokemon().location())
                        .externalId(FAKER.ancient().god())
                        .build()
        ));

        // When
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(),
                        symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"vcs_organization_id\" : %s\n" +
                                "}", organizationId1, vcsOrganizationEntities.get(0).getId()))
                        ));
        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"vcs_organization_id\" : %s\n" +
                                "}", organizationId2, vcsOrganizationEntities.get(1).getId()))
                        ));
        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"vcs_organization_id\" : %s\n" +
                                "}", organizationId3, vcsOrganizationEntities.get(2).getId()))
                        ));
    }

    @Test
    @Order(2)
    void should_start_data_cycle_time_update_job_for_organization_id_and_organization_settings() throws SymeoException, InterruptedException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final Organization organization = Organization.builder()
                .id(organizationId)
                .name(faker.dog().name())
                .build();
        final RepositoryEntity repositoryEntity = RepositoryEntity.builder()
                .id(faker.dog().name())
                .name(faker.ancient().god())
                .organizationId(organizationId)
                .vcsOrganizationId(organizationId.toString())
                .vcsOrganizationName(organization.getName())
                .build();
        repositoryRepository.save(repositoryEntity);

        final String deployMergeCommitSha1 = faker.gameOfThrones().dragon() + "-deploy";
        final String commitSha2 = faker.gameOfThrones().dragon() + "-commit-2";
        final String mergeCommitSha3 = faker.gameOfThrones().dragon() + "-commit-3";
        final String commitSha4 = faker.gameOfThrones().dragon() + "-commit-4";
        final String commitSha5 = faker.gameOfThrones().dragon() + "-commit-5";

        final String authorLogin = faker.harryPotter().character();

        final CommitEntity deployCommitEntity1 = CommitEntity.builder()
                .sha(deployMergeCommitSha1)
                .message(faker.rickAndMorty().character())
                .authorLogin(authorLogin)
                .repositoryId(repositoryEntity.getId())
                .parentShaList(List.of(commitSha2))
                .date(ofInstant(stringToDate("2022-01-30").toInstant(), ZoneId.systemDefault()))
                .build();
        final CommitEntity commitEntity2 = CommitEntity.builder()
                .sha(commitSha2)
                .message(faker.rickAndMorty().character())
                .authorLogin(authorLogin)
                .repositoryId(repositoryEntity.getId())
                .parentShaList(List.of(mergeCommitSha3))
                .date(ofInstant(stringToDate("2022-01-20").toInstant(), ZoneId.systemDefault()))
                .build();
        final CommitEntity mergeCommitEntity3 = CommitEntity.builder()
                .sha(mergeCommitSha3)
                .message(faker.rickAndMorty().character())
                .authorLogin(authorLogin)
                .repositoryId(repositoryEntity.getId())
                .parentShaList(List.of(commitSha4))
                .date(ofInstant(stringToDate("2022-01-15").toInstant(), ZoneId.systemDefault()))
                .build();
        final CommitEntity commitEntity4 = CommitEntity.builder()
                .sha(commitSha4)
                .message(faker.rickAndMorty().character())
                .authorLogin(authorLogin)
                .repositoryId(repositoryEntity.getId())
                .parentShaList(List.of(commitSha5))
                .date(ofInstant(stringToDate("2022-01-10").toInstant(), ZoneId.systemDefault()))
                .build();
        final CommitEntity commitEntity5 = CommitEntity.builder()
                .sha(commitSha5)
                .message(faker.rickAndMorty().character())
                .authorLogin(authorLogin)
                .repositoryId(repositoryEntity.getId())
                .parentShaList(List.of())
                .date(ofInstant(stringToDate("2022-01-01").toInstant(), ZoneId.systemDefault()))
                .build();
        commitRepository.saveAll(List.of(
                deployCommitEntity1,
                commitEntity2,
                mergeCommitEntity3,
                commitEntity4,
                commitEntity5
        ));

        final PullRequestEntity deployPullRequestEntity = PullRequestEntity.builder()
                .id(faker.gameOfThrones().character() + "-2")
                .code(String.valueOf(faker.number().numberBetween(1, 100)))
                .creationDate(ofInstant(stringToDate("2022-01-25").toInstant(), ZoneId.systemDefault()))
                .lastUpdateDate(ofInstant(stringToDate("2022-01-30").toInstant(), ZoneId.systemDefault()))
                .isDraft(false)
                .authorLogin(faker.harryPotter().character())
                .vcsRepositoryId(repositoryEntity.getId())
                .vcsUrl(faker.gameOfThrones().character() + "-url-1")
                .commitNumber(3)
                .deletedLineNumber(faker.number().numberBetween(1, 100))
                .addedLineNumber(faker.number().numberBetween(1, 100))
                .mergeDate(ofInstant(stringToDate("2022-01-30").toInstant(), ZoneId.systemDefault()))
                .mergeCommitSha(deployMergeCommitSha1)
                .isMerged(true)
                .title(faker.animal().name() + "-title-1")
                .vcsRepository(repositoryEntity.getName())
                .vcsOrganizationId(organizationId.toString())
                .organizationId(organizationId)
                .head("staging")
                .base("main")
                .commitShaList(List.of(deployMergeCommitSha1, commitSha2, mergeCommitSha3))
                .build();

        final PullRequestEntity pullRequestEntity2 = PullRequestEntity.builder()
                .id(faker.gameOfThrones().character() + "-1")
                .code(String.valueOf(faker.number().numberBetween(1, 100)))
                .creationDate(ofInstant(stringToDate("2022-01-01").toInstant(), ZoneId.systemDefault()))
                .lastUpdateDate(ofInstant(stringToDate("2022-01-15").toInstant(), ZoneId.systemDefault()))
                .isDraft(false)
                .authorLogin(faker.harryPotter().character())
                .vcsRepositoryId(repositoryEntity.getId())
                .vcsUrl(faker.gameOfThrones().character() + "-url-2")
                .commitNumber(3)
                .deletedLineNumber(faker.number().numberBetween(1, 100))
                .addedLineNumber(faker.number().numberBetween(1, 100))
                .mergeDate(ofInstant(stringToDate("2022-01-15").toInstant(), ZoneId.systemDefault()))
                .mergeCommitSha(mergeCommitSha3)
                .isMerged(true)
                .title(faker.animal().name() + "-title-2")
                .vcsRepository(repositoryEntity.getName())
                .vcsOrganizationId(organizationId.toString())
                .organizationId(organizationId)
                .head("feature/test")
                .base("staging")
                .commitShaList(List.of(mergeCommitSha3, commitSha4, commitSha5))
                .build();

        pullRequestRepository.saveAll(List.of(deployPullRequestEntity, pullRequestEntity2));


        final TagEntity deployTagEntity1 = TagEntity.builder()
                .sha(deployCommitEntity1.getSha())
                .name("deploy")
                .repositoryId(repositoryEntity.getId())
                .build();
        final TagEntity tagEntity2 = TagEntity.builder()
                .sha(mergeCommitEntity3.getSha())
                .name(faker.dog().name() + "-2")
                .repositoryId(repositoryEntity.getId())
                .build();
        tagRepository.saveAll(List.of(
                deployTagEntity1,
                tagEntity2
        ));

        final CycleTimeEntity cycleTimeEntityToReplace = CycleTimeEntity.builder()
                .id(faker.dragonBall().character() + "-current-1")
                .value(100L)
                .codingTime(200L)
                .reviewTime(300L)
                .timeToDeploy(400L)
                .deployDate(stringToDate("2022-02-15"))
                .pullRequestId(faker.rickAndMorty().character() + "-current-1")
                .pullRequestAuthorLogin(faker.name().firstName())
                .pullRequestMergeDate(stringToDate("2022-02-13"))
                .pullRequestUpdateDate(stringToDate("2022-02-13"))
                .pullRequestCreationDate(stringToDate("2022-02-10"))
                .pullRequestVcsRepositoryId(repositoryEntity.getId())
                .pullRequestVcsRepository(faker.howIMetYourMother().character())
                .pullRequestVcsUrl(faker.backToTheFuture().character())
                .pullRequestState("merge")
                .pullRequestTitle(faker.harryPotter().character())
                .pullRequestHead("feature/test-current-1")
                .build();
        cycleTimeRepository.save(cycleTimeEntityToReplace);

        final List<String> repositoryIds = List.of(repositoryEntity.getId());
        final String deployDetectionType = DeployDetectionSettingsContract.DeployDetectionTypeEnum.TAG.toString();
        final String pullRequestMergedOnBranchRegex = faker.gameOfThrones().character();
        final String tagRegex = "^deploy$";
        final String excludeBranchRegexes = "^staging$";

        final PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract body =
                new PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract();
        body.setOrganizationId(organizationId);
        body.setRepositoryIds(repositoryIds);
        body.setDeployDetectionType(deployDetectionType);
        body.setPullRequestMergedOnBranchRegex(pullRequestMergedOnBranchRegex);
        body.setTagRegex(tagRegex);
        body.setExcludeBranchRegexes(List.of(excludeBranchRegexes));

        // When
        client.post()
                .uri(getApiURI(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_CYCLE_TIMES))
                .body(Mono.just(body), PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.class)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(),
                        symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        Thread.sleep(5000);

        final List<JobEntity> jobEntities = jobRepository.findAll();
        assertThat(jobEntities.size()).isEqualTo(1);
        assertThat(jobEntities.get(0).getStatus()).isEqualTo(Job.FINISHED);

        final List<CycleTimeEntity> cycleTimeEntities = cycleTimeRepository.findAll();
        assertThat(cycleTimeEntities.size()).isEqualTo(1);
        assertThat(cycleTimeEntities.get(0).getId()).isEqualTo(pullRequestEntity2.getId());
    }

    @Test
    @Order(3)
    void should_return_unauthorized_response_for_wrong_api_key_for_all_data_collection_job() {
        // When
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.ancient().god())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(4)
    void should_return_unauthorized_response_for_wrong_api_key_for_job_given_an_organization() {
        // When
        client.post()
                .uri(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.gameOfThrones().character())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(5)
    void should_return_unauthorized_response_for_wrong_api_key_for_job_given_a_team() {
        // When
        client.post()
                .uri(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_TEAM)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.pokemon().name())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(6)
    void should_return_unauthorized_response_for_wrong_api_key_for_job_given_repositories() {
        // When
        client.post()
                .uri(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_REPOSITORIES)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.pokemon().name())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }


}
