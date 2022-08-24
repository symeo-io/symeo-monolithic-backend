package io.symeo.monolithic.backend.bootstrap.it.data;

import io.symeo.monolithic.backend.bootstrap.ITGithubJwtTokenProvider;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.runnable.CollectPullRequestsJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.installation.GithubInstallationAccessTokenDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.installation.GithubInstallationDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import io.symeo.monolithic.backend.infrastructure.json.local.storage.properties.JsonStorageProperties;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestTimeToMergeRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.apache.commons.codec.digest.HmacUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    public AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    @Autowired
    public JobStorage jobStorage;
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
    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();

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
        assertThat(vcsOrganizationEntities.get(0).getVcsId()).isEqualTo("github-" + githubWebhookEventDTO.getInstallation().getAccount().getId());
        assertThat(vcsOrganizationEntities.get(0).getExternalId()).isEqualTo(githubWebhookEventDTO.getInstallation().getId());
        assertThat(vcsOrganizationEntities.get(0).getOrganizationEntity().getName()).isEqualTo(organizationName);
        final List<JobEntity> jobs = jobRepository.findAll();
        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getOrganizationId()).isEqualTo(vcsOrganizationEntities.get(0).getOrganizationEntity().getId());
        assertThat(jobs.get(0).getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
    }

    @Order(2)
    @Test
    void should_collect_github_repositories_and_linked_pull_requests_for_a_given_organization() throws SymeoException, IOException, InterruptedException {
        // Given
        jsonStorageProperties.setRootDirectory(TMP_DIR);
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("github_stubs",
                "get_app_installations.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO = githubInstallationDTOS[0];
        final String organizationName = githubInstallationDTO.getAccount().getLogin();
        githubInstallationDTO.getAccount().setLogin(organizationName);
        final String githubTokenStub = FAKER.gameOfThrones().character();
        itGithubJwtTokenProvider.setGithubTokenStub(githubTokenStub);
        final String organizationVcsId = FAKER.rickAndMorty().character();
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().name(organizationName).vcsId(organizationVcsId).build())
                .id(organizationId)
                .name(organizationName)
                .build();
        accountOrganizationStorageAdapter.createOrganization(organization);

        wireMockServer.stubFor(
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

        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO.getId() + "/access_tokens"))
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
        wireMockServer.stubFor(
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
        final GithubPullRequestDTO[] githubPullRequestDTOS = updatePullRequestsDates(getStubsFromClassT("github_stubs"
                , "get_pr_repo_1.json",
                GithubPullRequestDTO[].class));
        wireMockServer.stubFor(
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
        wireMockServer.stubFor(
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

        wireMockServer.stubFor(
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
        wireMockServer.stubFor(
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
        wireMockServer.stubFor(
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
        wireMockServer.stubFor(
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


        // When
        client.get()
                .uri(getApiURI(DATA_PROCESSING_JOB_REST_API_GET_START_JOB, "organization_name", organizationName))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        Thread.sleep(2000L);
        final List<Job> repositoriesJobs =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(CollectRepositoriesJobRunnable.JOB_CODE, organization);
        assertThat(repositoriesJobs).hasSize(1);
        assertThat(repositoriesJobs.get(0).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(repositoriesJobs.get(0).getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        final List<Job> pullRequestsJobs =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(CollectPullRequestsJobRunnable.JOB_CODE, organization);
        assertThat(pullRequestsJobs).hasSize(1);
        assertThat(pullRequestsJobs.get(0).getStatus()).isEqualTo(Job.FINISHED);
        assertThat(pullRequestsJobs.get(0).getCode()).isEqualTo(CollectPullRequestsJobRunnable.JOB_CODE);
        final Path rawStorageOrganizationPath = Paths.get(TMP_DIR + "/" + organization.getId().toString());
        assertThat(Files.exists(rawStorageOrganizationPath)).isTrue();
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github"))).isTrue();
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github").resolve("repositories.json"))).isTrue();
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github").resolve("pull_requests_github-495382833" +
                ".json"))).isTrue();
        assertThat(Files.exists(rawStorageOrganizationPath.resolve("github").resolve("pull_requests_github-512630813" +
                ".json"))).isTrue();
        final List<RepositoryEntity> allRepositoryEntities = repositoryRepository.findAll();
        assertThat(allRepositoryEntities).hasSize(githubRepositoryDTOS.length);
        allRepositoryEntities.forEach(repositoryEntity -> assertThat(repositoryEntity.getOrganizationId()).isEqualTo(organization.getId()));
        final List<PullRequestEntity> allPullRequestEntities = pullRequestRepository.findAll();
        assertThat(allPullRequestEntities).hasSize(githubPullRequestDTOS.length);
        allPullRequestEntities.forEach(pullRequestEntity -> {
            assertThat(pullRequestEntity.getOrganizationId()).isEqualTo(organization.getId());
            assertThat(pullRequestEntity.getVcsRepositoryId()).isEqualTo("github-" + githubRepositoryDTOS[0].getId());
        });
    }

    @Order(3)
    @Test
    void should_return_pull_requests_linked_to_team_id() {
        // Given
        final List<String> repositoryIds =
                repositoryRepository.findAll().stream().map(RepositoryEntity::getId).toList();
        final TeamEntity teamEntity = TeamEntity.builder()
                .organizationId(organizationId)
                .id(teamId)
                .repositoryIds(repositoryIds)
                .name(FAKER.ancient().god())
                .build();
        teamRepository.save(teamEntity);

        // When
        final List<PullRequestEntity> allByOrganizationIdAndTeamId =
                pullRequestRepository.findAllByOrganizationIdAndTeamId(organizationId, teamId);

        // Then
        assertThat(allByOrganizationIdAndTeamId).hasSize(pullRequestRepository.findAll().size());
    }

    @Order(4)
    @Test
    void should_return_time_to_merge_view() {
        // When
        final List<PullRequestTimeToMergeDTO> timeToMergeDTOsByOrganizationIdAndTeamId =
                pullRequestTimeToMergeRepository.findTimeToMergeDTOsByOrganizationIdAndTeamId(organizationId, teamId);

        // Then
        assertThat(timeToMergeDTOsByOrganizationIdAndTeamId).hasSize(pullRequestRepository.findAll().size());
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
