package fr.catlean.monolithic.backend.bootstrap.it.data;

import fr.catlean.monolithic.backend.bootstrap.ITGithubJwtTokenProvider;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectPullRequestsJobRunnable;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.JobStorage;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.installation.GithubInstallationAccessTokenDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.installation.GithubInstallationDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import fr.catlean.monolithic.backend.infrastructure.json.local.storage.properties.JsonStorageProperties;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestTimeToMergeRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
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

public class CatleanGithubCollectionAndApiIT extends AbstractCatleanDataCollectionAndApiIT {

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
    void should_collect_github_repositories_and_linked_pull_requests_for_a_given_organization() throws CatleanException, IOException, InterruptedException {
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

    @Order(2)
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

    @Order(3)
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
