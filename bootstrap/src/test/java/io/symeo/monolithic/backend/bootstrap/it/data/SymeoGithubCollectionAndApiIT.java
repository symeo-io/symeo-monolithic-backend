package io.symeo.monolithic.backend.bootstrap.it.data;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.HmacUtils;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.bootstrap.ITGithubJwtTokenProvider;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForOrganizationContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForTeamContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForVcsOrganizationContract;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffJobStorage;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.organization.OrganizationSettingsService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import io.symeo.monolithic.backend.infrastructure.json.local.storage.properties.JsonStorageProperties;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubTagDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.installation.GithubInstallationAccessTokenDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.installation.GithubInstallationDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectVcsDataForRepositoriesAndDatesJobRunnable;
import io.symeo.monolithic.backend.job.domain.port.out.RawStorageAdapter;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SymeoGithubCollectionAndApiIT extends AbstractSymeoDataCollectionAndApiIT {

    @Autowired
    public OrganizationStorageAdapter organizationStorageAdapter;

    @Autowired
    public RawStorageAdapter rawStorageAdapter;
    @Autowired
    public BffJobStorage jobStorage;
    @Autowired
    public ITGithubJwtTokenProvider itGithubJwtTokenProvider;
    @Autowired
    public GithubProperties githubProperties;
    @Autowired
    public JsonStorageProperties jsonStorageProperties;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public PullRequestRepository pullRequestRepository;
    @Autowired
    public PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public GithubWebhookProperties githubWebhookProperties;
    @Autowired
    public VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    public JobRepository jobRepository;
    @Autowired
    public CommitRepository commitRepository;
    @Autowired
    public OrganizationSettingsRepository organizationSettingsRepository;
    @Autowired
    public TagRepository tagRepository;
    @Autowired
    public SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;
    @Autowired
    public OrganizationSettingsService organizationSettingsService;
    private static final UUID firstOrganizationId = UUID.randomUUID();
    private static final UUID secondOrganizationId = UUID.randomUUID();
    private static final UUID firstTeamId = UUID.randomUUID();
    private static final UUID secondTeamId = UUID.randomUUID();

    private static String TMP_DIR;

    @BeforeAll
    static void beforeAll() throws IOException {
        TMP_DIR = Files.createTempDirectory("json_local_storage_adapter_integration_test").toFile().getAbsolutePath();
    }

    @AfterAll
    static void afterAll() {
        new File(TMP_DIR).delete();
    }


    @Order(1)
    @Test
    public void should_create_an_organization_triggered_by_a_github_webhook_installation_event() throws IOException {
        // Given
        final byte[] githubWebhookInstallationCreatedEventBytes = Files.readString(Paths.get("target/test-classes" +
                "/webhook_events" +
                "/post_installation_created_1.json")).getBytes();
        final String hmacSHA256 = new HmacUtils("HmacSHA256",
                githubWebhookProperties.getSecret()).hmacHex(githubWebhookInstallationCreatedEventBytes);

        // When
        client
                .post()
                .uri(getApiURI(GITHUB_WEBHOOK_API))
                .bodyValue(githubWebhookInstallationCreatedEventBytes)
                .header("X-GitHub-Event", "installation")
                .header("X-Hub-Signature-256", "sha256=" + hmacSHA256)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        final List<VcsOrganizationEntity> vcsOrganizationEntities = vcsOrganizationRepository.findAll();
        assertThat(vcsOrganizationEntities).hasSize(1);
        final GithubWebhookEventDTO githubWebhookEventDTO =
                objectMapper.readValue(githubWebhookInstallationCreatedEventBytes, GithubWebhookEventDTO.class);
        final String organizationName = githubWebhookEventDTO.getInstallation().getAccount().getLogin();
        assertThat(vcsOrganizationEntities.get(0).getName()).isEqualTo(organizationName);
        assertThat(vcsOrganizationEntities.get(0).getVcsId()).isEqualTo("github-" + githubWebhookEventDTO
                .getInstallation().getAccount().getId());
        assertThat(vcsOrganizationEntities.get(0).getExternalId()).isEqualTo(githubWebhookEventDTO.getInstallation
                ().getId());
        assertThat(vcsOrganizationEntities.get(0).getOrganizationEntity().getName()).isEqualTo(organizationName);
        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                        "  \"organization_id\" : \"%s\",\n" +
                                        "  \"vcs_organization_id\" : %s\n" +
                                        "}", vcsOrganizationEntities.get(0).getOrganizationId(),
                                vcsOrganizationEntities.get(0).getId()))
                        ));
    }

    @Order(2)
    @Test
    void should_collect_github_repositories_for_a_given_organization() throws SymeoException,
            InterruptedException, IOException {
        // Given
        jsonStorageProperties.setRootDirectory(TMP_DIR);
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("github_stubs",
                "get_app_installations.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO = githubInstallationDTOS[0];
        final String organizationName = githubInstallationDTO.getAccount().getLogin();
        final String githubTokenStub = FAKER.gameOfThrones().character();
        itGithubJwtTokenProvider.setGithubTokenStub(githubTokenStub);
        final String organizationVcsId = FAKER.rickAndMorty().character();
        final Organization organization = Organization.builder()
                .vcsOrganization(Organization.VcsOrganization.builder()
                        .name(organizationName)
                        .vcsId(organizationVcsId)
                        .externalId(FAKER.ancient().god())
                        .build())
                .id(firstOrganizationId)
                .name(organizationName)
                .build();
        final Organization organizationSaved = organizationStorageAdapter.createOrganization(organization);
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organizationSaved);

        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo("/app/installations"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubTokenStub))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationDTOS,
                                        200)));
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO = getStubsFromClassT(
                "github_stubs",
                "post_app_installation_1.json",
                GithubInstallationAccessTokenDTO.class);

        symeoClientAdapterWireMockServer.stubFor(
                post(urlEqualTo("/app/installations/" + githubInstallationDTO.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubTokenStub))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO,
                                        200)));


        final GithubRepositoryDTO[] githubRepositoryDTOS = updateRepositoryOrganization(getStubsFromClassT(
                "github_stubs",
                "get_repositories_page_0.json",
                GithubRepositoryDTO[].class), organizationName);
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/orgs/%s/repos?sort=name&per_page=%s&page=%s", organizationName,
                                githubProperties.getSize(), "1")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        githubRepositoryDTOS, 200
                                )
                        )
        );

        final PostStartDataProcessingJobForVcsOrganizationContract body =
                new PostStartDataProcessingJobForVcsOrganizationContract();
        body.setOrganizationId(organization.getId());
        body.setVcsOrganizationId(organizationSaved.getVcsOrganization().getId());

        // When
        client.post()
                .uri(getApiURI(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION))
                .body(Mono.just(body), PostStartDataProcessingJobForVcsOrganizationContract.class)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(),
                        symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        Thread.sleep(2000L);
        final List<JobView> repositoriesJobs =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(CollectRepositoriesJobRunnable.JOB_CODE, organization);
        assertThat(repositoriesJobs).hasSize(1);
        assertThat(repositoriesJobs.get(0).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(repositoriesJobs.get(0).getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        final Path rawStorageOrganizationPath = Paths.get(TMP_DIR + "/" + organization.getId().toString());
        assertThat(Files.exists(rawStorageOrganizationPath)).isTrue();
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github"))).isTrue();
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github").resolve("repositories.json"))).isTrue();
        final List<RepositoryEntity> allRepositoryEntities = repositoryRepository.findAll();
        assertThat(allRepositoryEntities).hasSize(githubRepositoryDTOS.length);
        allRepositoryEntities.forEach(repositoryEntity -> assertThat(repositoryEntity.getOrganizationId())
                .isEqualTo(organization.getId()));

    }

    //    @Order(3)
//    @Test
    void should_collect_github_vcs_data_for_a_given_organization_and_reposity_ids() throws IOException, SymeoException,
            InterruptedException {
        // Given
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("github_stubs",
                "get_app_installations.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO = githubInstallationDTOS[0];
        final String organizationName = githubInstallationDTO.getAccount().getLogin();
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO = getStubsFromClassT(
                "github_stubs",
                "post_app_installation_1.json",
                GithubInstallationAccessTokenDTO.class);
        final GithubRepositoryDTO[] githubRepositoryDTOS = updateRepositoryOrganization(getStubsFromClassT(
                "github_stubs",
                "get_repositories_page_0.json",
                GithubRepositoryDTO[].class), organizationName);
        final GithubPullRequestDTO[] githubPullRequestDTOS = updatePullRequestsDates(getStubsFromClassT("github_stubs"
                , "get_pr_repo_1.json",
                GithubPullRequestDTO[].class));
        final Organization organization = organizationStorageAdapter.findOrganizationById(firstOrganizationId).get();
        final List<String> repositoryIds = List.of(repositoryRepository.findAll().get(0).getId());
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(firstOrganizationId)
                        .name(FAKER.name().firstName())
                        .id(firstTeamId)
                        .repositoryIds(repositoryIds)
                        .build()
        );

        // When
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls?sort=updated&direction=desc&state=all&per_page" +
                                        "=%s" +
                                        "&page=%s", organizationName, githubRepositoryDTOS[0].getName(),
                                githubProperties.getSize(), "1")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        githubPullRequestDTOS, 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls?sort=updated&direction=desc&state=all&per_page" +
                                        "=%s" +
                                        "&page=%s", organizationName, githubRepositoryDTOS[1].getName(),
                                githubProperties.getSize(), "1")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        "[]", 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[0].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_1.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[0].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_1_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[0].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_1_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );

        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[1].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_2.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[1].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_2_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[1].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_2_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[2].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_3.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[2].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_3_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[2].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_3_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[3].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_4.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[3].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_4_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                githubRepositoryDTOS[0].getName(), githubPullRequestDTOS[3].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_4_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/branches?per_page=%s&page=%s", organizationName,
                                githubRepositoryDTOS[0].getName(), githubProperties.getSize(), "1")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_branches_1.json",
                                                GithubBranchDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/commits?page=%s&per_page=%s&sha=%s", organizationName,
                                githubRepositoryDTOS[0].getName(), "1", githubProperties.getSize(),
                                "add-update-organization-settings")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_orga_branch_1_commits_1.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/commits?page=%s&per_page=%s&sha=%s", organizationName,
                                githubRepositoryDTOS[0].getName(), "2", githubProperties.getSize(),
                                "add-update-organization-settings")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        "[]", 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/git/matching-refs/tags", organizationName,
                                githubRepositoryDTOS[0].getName())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_tags.json",
                                                GithubTagDTO[].class), 200
                                )
                        )
        );

        final PostStartDataProcessingJobForOrganizationContract body =
                new PostStartDataProcessingJobForOrganizationContract();
        body.setOrganizationId(firstOrganizationId);
        body.setRepositoryIds(repositoryIds);

        client.post()
                .uri(getApiURI(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_REPOSITORIES))
                .body(Mono.just(body), PostStartDataProcessingJobForOrganizationContract.class)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(),
                        symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        Thread.sleep(2000);

        // Then
        final List<JobView> pullRequestsJobs =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc
                        (CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE, organization);
        assertThat(pullRequestsJobs).hasSize(2);
        assertThat(pullRequestsJobs.get(0).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(pullRequestsJobs.get(0).getCode()).isEqualTo(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE);
        assertThat(pullRequestsJobs.get(0).getOrganizationId()).isEqualTo(firstOrganizationId);
        final Path rawStorageOrganizationPath = Paths.get(TMP_DIR + "/" + organization.getId().toString());
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github").resolve
                ("pull_requests_github-495382833" +
                        ".json"))).isTrue();
        final List<PullRequestEntity> allPullRequestEntities = pullRequestRepository.findAll();
        assertThat(allPullRequestEntities).hasSize(githubPullRequestDTOS.length);
        allPullRequestEntities.forEach(pullRequestEntity -> {
            assertThat(pullRequestEntity.getOrganizationId()).isEqualTo(organization.getId());
            assertThat(pullRequestEntity.getVcsRepositoryId()).isEqualTo("github-" + githubRepositoryDTOS[0].getId());
        });
        final List<CommitEntity> commitEntities = commitRepository.findAll();
        assertThat(commitEntities).hasSize(100);

        final List<OrganizationSettingsEntity> organizationSettings = organizationSettingsRepository.findAll();
        assertThat(organizationSettings).hasSize(1);
        assertThat(organizationSettings.get(0).getOrganizationId()).isEqualTo(organization.getId());

        final List<TagEntity> tagsForOrganizationAndRepository = tagRepository.findAll();
        assertThat(tagsForOrganizationAndRepository).hasSize(69);
        assertThat(tagsForOrganizationAndRepository.get(0).getSha()).isEqualTo
                ("817245d0b26d7a252327c60da4eff4469ab0d9ab");
        assertThat(tagsForOrganizationAndRepository.get(1).getSha()).isEqualTo
                ("c690fa6d589a1414b0f3fd2b980e07c57dc4dd15");
        assertThat(tagsForOrganizationAndRepository.get(2).getSha()).isEqualTo
                ("32228d8700be8974161d0541ad04d0ea1932ddc2");
    }

    //    @Order(4)
//    @Test
    void should_collect_github_vcs_data_for_a_given_organization_and_team() throws IOException, SymeoException,
            InterruptedException {

        // Given
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("github_stubs",
                "get_app_installations_2.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO = githubInstallationDTOS[0];
        final String organizationName = githubInstallationDTO.getAccount().getLogin();
        final String organizationVcsId = FAKER.gameOfThrones().character();
        final Organization organization = Organization.builder()
                .vcsOrganization(Organization.VcsOrganization.builder().name(organizationName).vcsId(organizationVcsId).build())
                .id(secondOrganizationId)
                .name(organizationName)
                .build();
        organizationStorageAdapter.createOrganization(organization);

        final RepositoryEntity repositoryEntity = RepositoryEntity.builder()
                .id(FAKER.cat().name())
                .name("symeo-job-test")
                .vcsOrganizationId(FAKER.gameOfThrones().character())
                .vcsOrganizationName(organizationName)
                .organizationId(secondOrganizationId)
                .defaultBranch("staging")
                .build();
        repositoryRepository.save(repositoryEntity);

        final List<String> repositoryIds = List.of(repositoryRepository.findAll().get(2).getId());
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(secondOrganizationId)
                        .name(FAKER.name().firstName())
                        .id(secondTeamId)
                        .repositoryIds(repositoryIds)
                        .build()
        );


        final String githubTokenStub = FAKER.gameOfThrones().character();
        itGithubJwtTokenProvider.setGithubTokenStub(githubTokenStub);
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo("/app/installations"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubTokenStub))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationDTOS,
                                        200)));


        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO = getStubsFromClassT(
                "github_stubs",
                "post_app_installation_2.json",
                GithubInstallationAccessTokenDTO.class);
        symeoClientAdapterWireMockServer.stubFor(
                post(urlEqualTo("/app/installations/" + githubInstallationDTO.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubTokenStub))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO,
                                        200)));

        final GithubRepositoryDTO[] githubRepositoryDTOS = updateRepositoryOrganization(getStubsFromClassT(
                "github_stubs",
                "get_repositories_page_0.json",
                GithubRepositoryDTO[].class), organizationName);
        final GithubPullRequestDTO[] githubPullRequestDTOS = updatePullRequestsDates(getStubsFromClassT("github_stubs"
                , "get_pr_repo_1.json",
                GithubPullRequestDTO[].class));

        // When
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls?sort=updated&direction=desc&state=all&per_page" +
                                        "=%s" +
                                        "&page=%s", organizationName, repositoryEntity.getName(),
                                githubProperties.getSize(), "1")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        githubPullRequestDTOS, 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[0].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_1.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[0].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_1_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[0].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_1_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );

        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[1].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_2.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[1].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_2_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[1].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_2_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[2].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_3.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[2].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_3_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[2].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_3_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s", organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[3].getNumber())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_details_4.json",
                                                GithubPullRequestDTO.class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/commits?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[3].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_4_commits.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/pulls/%s/comments?page=%s&per_page=%s",
                                organizationName,
                                repositoryEntity.getName(), githubPullRequestDTOS[3].getNumber(), "1",
                                githubProperties.getSize())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_pr_4_comments.json",
                                                GithubCommentsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/branches?per_page=%s&page=%s", organization
                                        .getVcsOrganization().getName(),
                                repositoryEntity.getName(), githubProperties.getSize(), "1")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_branches_2.json",
                                                GithubBranchDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/commits?page=%s&per_page=%s&sha=%s", organization
                                        .getVcsOrganization().getName(),
                                repositoryEntity.getName(), "1", githubProperties.getSize(), "aws")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_orga_branch_1_commits_1.json",
                                                GithubCommitsDTO[].class), 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/commits?page=%s&per_page=%s&sha=%s", organization
                                        .getVcsOrganization().getName(),
                                repositoryEntity.getName(), "2", githubProperties.getSize(), "aws")))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        "[]", 200
                                )
                        )
        );
        symeoClientAdapterWireMockServer.stubFor(
                get(
                        urlEqualTo(String.format("/repos/%s/%s/git/matching-refs/tags", organization
                                        .getVcsOrganization().getName(),
                                repositoryEntity.getName())))
                        .withHeader("Authorization", equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getStubsFromClassT("github_stubs", "get_repo_1_tags_2.json",
                                                GithubTagDTO[].class), 200
                                )
                        )
        );
        final PostStartDataProcessingJobForTeamContract body =
                new PostStartDataProcessingJobForTeamContract();
        body.setOrganizationId(secondOrganizationId);
        body.setRepositoryIds(repositoryIds);
        body.setTeamId(secondTeamId);

        client.post()
                .uri(getApiURI(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_TEAM))
                .body(Mono.just(body), PostStartDataProcessingJobForTeamContract.class)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(),
                        symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        Thread.sleep(2000);

        final List<JobView> pullRequestsJobs =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc
                        (CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE, organization);
        assertThat(pullRequestsJobs).hasSize(1);
        assertThat(pullRequestsJobs.get(0).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(pullRequestsJobs.get(0).getCode()).isEqualTo(CollectVcsDataForRepositoriesAndDatesJobRunnable
                .JOB_CODE);
        assertThat(pullRequestsJobs.get(0).getOrganizationId()).isEqualTo(secondOrganizationId);
        final Path rawStorageOrganizationPath = Paths.get(TMP_DIR + "/" + organization.getId().toString());
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github").resolve("pull_requests_" +
                repositoryEntity.getId() +
                ".json"))).isTrue();
        final List<PullRequestEntity> allPullRequestEntities = pullRequestRepository.findAll();
        assertThat(allPullRequestEntities).hasSize(githubPullRequestDTOS.length);
        allPullRequestEntities.forEach(pullRequestEntity -> {
            assertThat(pullRequestEntity.getOrganizationId()).isEqualTo(organization.getId());
            assertThat(pullRequestEntity.getVcsRepositoryId()).isEqualTo("github-" + githubRepositoryDTOS[0].getId());
        });
        final List<CommitEntity> commitEntities = commitRepository.findAll();
        assertThat(commitEntities).hasSize(100);

        final List<OrganizationSettingsEntity> organizationSettings = organizationSettingsRepository.findAll();
        assertThat(organizationSettings).hasSize(2);
        assertThat(organizationSettings.get(1).getOrganizationId()).isEqualTo(organization.getId());

        final List<TagEntity> tagsForOrganizationAndRepositoryAndTeam = tagRepository.findAllForTeamId(secondTeamId);
        assertThat(tagsForOrganizationAndRepositoryAndTeam).hasSize(2);
        assertThat(tagsForOrganizationAndRepositoryAndTeam.get(0).getSha()).isEqualTo
                ("817245d0b26d7a252327c60da4eff4469ab0d9ab");
        assertThat(tagsForOrganizationAndRepositoryAndTeam.get(1).getSha()).isEqualTo
                ("c690fa6d589a1414b0f3fd2b980e07c57dc4dd15");
    }

    private static GithubRepositoryDTO[] updateRepositoryOrganization(GithubRepositoryDTO[] githubRepositoryDTOS,
                                                                      String organizationName) {
        final List<GithubRepositoryDTO> githubRepositoryDTOList = new ArrayList<>();
        for (GithubRepositoryDTO githubRepositoryDTO : githubRepositoryDTOS) {
            githubRepositoryDTO.getOwner().setLogin(organizationName);
            githubRepositoryDTOList.add(githubRepositoryDTO);
        }
        return githubRepositoryDTOList.toArray(new GithubRepositoryDTO[githubRepositoryDTOS.length]);
    }

    private static GithubPullRequestDTO[] updatePullRequestsDates(GithubPullRequestDTO[] githubPullRequestDTOS) {
        final List<GithubPullRequestDTO> githubPullRequestDTOSList = new ArrayList<>();
        for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestDTOS) {
            githubPullRequestDTO.setCreatedAt(new DateTime(new Date()).minusDays(5).toDate());
            githubPullRequestDTO.setMergedAt(new DateTime(new Date()).minusDays(3).toDate());
            githubPullRequestDTOSList.add(githubPullRequestDTO);
        }
        return githubPullRequestDTOSList.toArray(new GithubPullRequestDTO[githubPullRequestDTOS.length]);
    }
}
