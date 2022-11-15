package io.symeo.monolithic.backend.bootstrap.it.bff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.bff.contract.api.model.CreateTeamRequestContract;
import io.symeo.monolithic.backend.bff.contract.api.model.LinkOrganizationToCurrentUserRequestContract;
import io.symeo.monolithic.backend.bff.contract.api.model.UpdateOnboardingRequestContract;
import io.symeo.monolithic.backend.bff.contract.api.model.UpdateTeamRequestContract;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.service.organization.OrganizationService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.ORGANISATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

public class SymeoUserOnboardingApiIT extends AbstractSymeoBackForFrontendApiIT {

    @Autowired
    public VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    public ObjectMapper objectMapper;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public JobRepository jobRepository;
    @Autowired
    public OrganizationService organizationService;
    @Autowired
    public SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;
    private static final UUID organizationId = UUID.randomUUID();

    private static final String mail = faker.name().firstName() + "@" + faker.name().firstName() + ".fr";

    @AfterEach
    public void tearDown() {
        this.bffWireMockServer.resetAll();
    }

    @Order(1)
    @Test
    void should_create_me() {
        // Given
        authenticationContextProvider.authorizeUserForMail(mail);

        // When
        client.get()
                .uri(getApiURI(USER_REST_API_GET_ME))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.user.email").isEqualTo(mail)
                .jsonPath("$.user.id").isNotEmpty()
                .jsonPath("$.user.onboarding.has_configured_team").isEqualTo(false)
                .jsonPath("$.user.onboarding.has_connected_to_vcs").isEqualTo(false)
                .jsonPath("$.user.organization").isEmpty();
    }

    @Order(2)
    @Test
    void should_linked_organization_to_user() throws SymeoException {
        // Given
        final String organizationName = faker.ancient().hero();
        organizationService.createOrganization(
                Organization.builder()
                        .id(organizationId)
                        .name(organizationName)
                        .vcsOrganization(
                                Organization.VcsOrganization.builder()
                                        .vcsId(faker.rickAndMorty().character())
                                        .name(organizationName).build())
                        .build()
        );
        final LinkOrganizationToCurrentUserRequestContract requestContract =
                new LinkOrganizationToCurrentUserRequestContract();
        final VcsOrganizationEntity vcsOrganizationEntity = vcsOrganizationRepository.findAll().get(0);
        requestContract.setExternalId(vcsOrganizationEntity.getExternalId());

        // When
        client.post()
                .uri(getApiURI(USER_REST_API_POST_ME_ORGANIZATION))
                .body(BodyInserters.fromValue(requestContract))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.user.email").isEqualTo(mail)
                .jsonPath("$.user.id").isNotEmpty()
                .jsonPath("$.user.onboarding.has_configured_team").isEqualTo(false)
                .jsonPath("$.user.onboarding.has_connected_to_vcs").isEqualTo(true)
                .jsonPath("$.user.organization.id").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId().toString())
                .jsonPath("$.user.organization.name").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getName());
    }

    @Order(3)
    @Test
    void should_get_me_with_organization() {
        // Given
        authenticationContextProvider.authorizeUserForMail(mail);
        final VcsOrganizationEntity vcsOrganizationEntity = vcsOrganizationRepository.findAll().get(0);

        // When
        client.get()
                .uri(getApiURI(USER_REST_API_GET_ME))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.user.email").isEqualTo(mail)
                .jsonPath("$.user.id").isNotEmpty()
                .jsonPath("$.user.onboarding.has_configured_team").isEqualTo(false)
                .jsonPath("$.user.onboarding.has_connected_to_vcs").isEqualTo(true)
                .jsonPath("$.user.organization.id").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId().toString())
                .jsonPath("$.user.organization.name").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getName());
    }


    @Order(4)
    @Test
    void should_get_repositories() {
        // Given
        final VcsOrganizationEntity vcsOrganizationEntity = vcsOrganizationRepository.findAll().get(0);
        repositoryRepository.saveAll(List.of(
                buildRepository(1, vcsOrganizationEntity),
                buildRepository(2, vcsOrganizationEntity),
                buildRepository(3, vcsOrganizationEntity),
                buildRepository(4, vcsOrganizationEntity)
        ));
        final List<RepositoryEntity> allRepositories = repositoryRepository.findAll();

        // When
        client.get()
                .uri(getApiURI(REPOSITORIES_REST_API_GET))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.repositories[0].name").isEqualTo(allRepositories.get(0).getName())
                .jsonPath("$.repositories[0].id").isEqualTo(allRepositories.get(0).getId())
                .jsonPath("$.repositories[1].name").isEqualTo(allRepositories.get(1).getName())
                .jsonPath("$.repositories[1].id").isEqualTo(allRepositories.get(1).getId())
                .jsonPath("$.repositories[2].name").isEqualTo(allRepositories.get(2).getName())
                .jsonPath("$.repositories[2].id").isEqualTo(allRepositories.get(2).getId())
                .jsonPath("$.repositories[3].name").isEqualTo(allRepositories.get(3).getName())
                .jsonPath("$.repositories[3].id").isEqualTo(allRepositories.get(3).getId());
    }


    @Order(5)
    @Test
    void should_configure_two_teams() throws InterruptedException {
        // Given
        final List<RepositoryEntity> allRepositories = repositoryRepository.findAll();
        final CreateTeamRequestContract requestContract1 = new CreateTeamRequestContract();
        requestContract1.setName(faker.rickAndMorty().character());
        requestContract1.setRepositoryIds(List.of(allRepositories.get(0).getId(), allRepositories.get(1).getId()));
        final CreateTeamRequestContract requestContract2 = new CreateTeamRequestContract();
        requestContract2.setName(faker.dragonBall().character());
        requestContract2.setRepositoryIds(List.of(allRepositories.get(2).getId(), allRepositories.get(3).getId()));

        // When
        final WebTestClient.ResponseSpec xxSuccessful = client.post()
                .uri(getApiURI(TEAM_REST_API))
                .body(BodyInserters.fromValue(List.of(requestContract1, requestContract2)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        final List<TeamEntity> teams = teamRepository.findAll();
        assertThat(teams).hasSize(2);
        xxSuccessful
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.teams[0].id").isEqualTo(teams.get(0).getId().toString())
                .jsonPath("$.teams[0].name").isEqualTo(teams.get(0).getName())
                .jsonPath("$.teams[0].repository_ids[0]").isEqualTo(teams.get(0).getRepositoryIds().get(0))
                .jsonPath("$.teams[0].repository_ids[1]").isEqualTo(teams.get(0).getRepositoryIds().get(1))
                .jsonPath("$.teams[0].name").isNotEmpty()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.teams[1].id").isEqualTo(teams.get(1).getId().toString())
                .jsonPath("$.teams[1].name").isEqualTo(teams.get(1).getName())
                .jsonPath("$.teams[1].repository_ids[0]").isEqualTo(teams.get(1).getRepositoryIds().get(0))
                .jsonPath("$.teams[1].repository_ids[1]").isEqualTo(teams.get(1).getRepositoryIds().get(1))
                .jsonPath("$.teams[1].name").isNotEmpty();
        Thread.sleep(2000);

        final DeployDetectionSettings defaultDeployDetectionSettings = DeployDetectionSettings.builder().build();

        bffWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_TEAM)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson("{\"organization_id\":\"" + organizationId + "\"," +
                                "\"team_id\":\"" + teams.get(0).getId() + "\",\"repository_ids\":[\"" + teams.get(0).getRepositoryIds().get(0) + "\"," +
                                "\"" + teams.get(0).getRepositoryIds().get(1) + "\"]," +
                                "\"deploy_detection_type\":" + "\"" + defaultDeployDetectionSettings.getDeployDetectionType().value + "\"," +
                                "\"tag_regex\": null," +
                                "\"exclude_branch_regexes\": [\"" + defaultDeployDetectionSettings.getExcludeBranchRegexes().get(0) + "\",\"" + defaultDeployDetectionSettings.getExcludeBranchRegexes().get(1) + "\"]," +
                                "\"pull_request_merged_on_branch_regex\":" + "\"" + defaultDeployDetectionSettings.getPullRequestMergedOnBranchRegex() + "\"" +
                                "}"))
        );
        bffWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_TEAM)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson("{\"organization_id\":\"" + organizationId + "\"," +
                                "\"team_id\":\"" + teams.get(1).getId() + "\",\"repository_ids\":[\"" + teams.get(1).getRepositoryIds().get(0) + "\"," +
                                "\"" + teams.get(1).getRepositoryIds().get(1) + "\"]," +
                                "\"deploy_detection_type\":" + "\"" + defaultDeployDetectionSettings.getDeployDetectionType().value + "\"," +
                                "\"tag_regex\": null," +
                                "\"exclude_branch_regexes\": [\"" + defaultDeployDetectionSettings.getExcludeBranchRegexes().get(0) + "\",\"" + defaultDeployDetectionSettings.getExcludeBranchRegexes().get(1) + "\"]," +
                                "\"pull_request_merged_on_branch_regex\":" + "\"" + defaultDeployDetectionSettings.getPullRequestMergedOnBranchRegex() + "\"" +
                                "}"))
        );
    }

    @Order(8)
    @Test
    void should_get_teams() {
        // When
        final WebTestClient.ResponseSpec xxSuccessful = client.get()
                .uri(getApiURI(TEAM_REST_API))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        final List<TeamEntity> teams = teamRepository.findAll();
        assertThat(teams).hasSize(2);
        xxSuccessful
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.teams[0].id").isEqualTo(teams.get(0).getId().toString())
                .jsonPath("$.teams[0].name").isEqualTo(teams.get(0).getName())
                .jsonPath("$.teams[0].repository_ids[0]").isEqualTo(teams.get(0).getRepositoryIds().get(0))
                .jsonPath("$.teams[0].repository_ids[1]").isEqualTo(teams.get(0).getRepositoryIds().get(1))
                .jsonPath("$.teams[0].name").isNotEmpty()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.teams[1].id").isEqualTo(teams.get(1).getId().toString())
                .jsonPath("$.teams[1].name").isEqualTo(teams.get(1).getName())
                .jsonPath("$.teams[1].repository_ids[0]").isEqualTo(teams.get(1).getRepositoryIds().get(0))
                .jsonPath("$.teams[1].repository_ids[1]").isEqualTo(teams.get(1).getRepositoryIds().get(1))
                .jsonPath("$.teams[1].name").isNotEmpty();

    }

    @Order(9)
    @Test
    void should_get_me_after_team_creation() {
        // Given
        authenticationContextProvider.authorizeUserForMail(mail);
        final VcsOrganizationEntity vcsOrganizationEntity = vcsOrganizationRepository.findAll().get(0);

        // When
        client.get()
                .uri(getApiURI(USER_REST_API_GET_ME))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.user.email").isEqualTo(mail)
                .jsonPath("$.user.id").isNotEmpty()
                .jsonPath("$.user.onboarding.has_configured_team").isEqualTo(true)
                .jsonPath("$.user.onboarding.has_connected_to_vcs").isEqualTo(true)
                .jsonPath("$.user.organization.id").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId().toString())
                .jsonPath("$.user.organization.name").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getName());
    }


    @Order(10)
    @Test
    void should_update_onboarding() {
        // Given
        final UpdateOnboardingRequestContract updateOnboardingRequestContract = new UpdateOnboardingRequestContract();
        updateOnboardingRequestContract.setHasConfiguredTeam(false);
        updateOnboardingRequestContract.setHasConnectedToVcs(false);

        // When
        client.post()
                .uri(getApiURI(USER_REST_API_POST_ME_ONBOARDING))
                .body(BodyInserters.fromValue(updateOnboardingRequestContract))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.onboarding.has_configured_team").isEqualTo(false)
                .jsonPath("$.onboarding.has_connected_to_vcs").isEqualTo(false)
                .jsonPath("$.onboarding.id").isNotEmpty();
    }

    @Order(11)
    @Test
    void should_delete_on_team() {
        // Given
        final List<TeamEntity> teams = teamRepository.findAll();
        final TeamEntity teamToDelete = teams.get(0);

        // When
        client.delete()
                .uri(getApiURI(TEAM_REST_API, "team_id", teamToDelete.getId().toString()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .isEmpty();
        final List<TeamEntity> teamsAfterDelete = teamRepository.findAll();
        assertThat(teamsAfterDelete).hasSize(1);
        assertThat(teamsAfterDelete.get(0).getId()).isNotEqualTo(teamToDelete.getId());
    }

    @Order(12)
    @Test
    void should_update_team_and_launch_job_on_updated_team() {
        // Given
        final List<TeamEntity> teams = teamRepository.findAll();
        final TeamEntity teamEntity = teams.get(0);
        final UpdateTeamRequestContract updateTeamRequestContract = new UpdateTeamRequestContract();
        updateTeamRequestContract.setId(teamEntity.getId());
        final String newName = faker.pokemon().name();
        updateTeamRequestContract.setName(newName);
        final List<String> newRepositoryIds = teamEntity.getRepositoryIds().subList(0, 1);
        updateTeamRequestContract.setRepositoryIds(newRepositoryIds);

        // When
        client.patch()
                .uri(getApiURI(TEAM_REST_API))
                .body(BodyInserters.fromValue(updateTeamRequestContract))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .isEmpty();
        final List<TeamEntity> teamsAfterUpdate = teamRepository.findAll();
        assertThat(teamsAfterUpdate).hasSize(1);
        assertThat(teamsAfterUpdate.get(0).getId()).isEqualTo(teamEntity.getId());
        assertThat(teamsAfterUpdate.get(0).getName()).isEqualTo(newName);
        assertThat(teamsAfterUpdate.get(0).getRepositoryIds()).hasSize(newRepositoryIds.size());
        teamsAfterUpdate.get(0).getRepositoryIds().forEach(repositoryId -> assertThat(newRepositoryIds.contains(repositoryId)).isTrue());
        final DeployDetectionSettings defaultDeployDetectionSettings = DeployDetectionSettings.builder().build();
        bffWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_TEAM)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson("{\"organization_id\":\"" + organizationId + "\"," +
                                "\"team_id\":\"" + teamsAfterUpdate.get(0).getId() + "\",\"repository_ids\":[\"" + teamsAfterUpdate.get(0).getRepositoryIds().get(0) + "\"]," +
                                "\"deploy_detection_type\":" + "\"" + defaultDeployDetectionSettings.getDeployDetectionType().value + "\"," +
                                "\"tag_regex\": null," +
                                "\"exclude_branch_regexes\": [\"" + defaultDeployDetectionSettings.getExcludeBranchRegexes().get(0) + "\",\"" + defaultDeployDetectionSettings.getExcludeBranchRegexes().get(1) + "\"]," +
                                "\"pull_request_merged_on_branch_regex\":" + "\"" + defaultDeployDetectionSettings.getPullRequestMergedOnBranchRegex() + "\"" +
                                "}"))
        );
    }

    @Order(13)
    @Test
    void should_return_empty_repositories() {
        // Given
        teamRepository.deleteAll();
        repositoryRepository.deleteAll();

        // When
        client.get()
                .uri(getApiURI(REPOSITORIES_REST_API_GET))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.repositories").isEmpty();
    }

    @Order(14)
    @Test
    void should_return_error_for_not_found_organization_to_link_to_current_user() {
        // Given
        final LinkOrganizationToCurrentUserRequestContract requestContract =
                new LinkOrganizationToCurrentUserRequestContract();
        final String externalId = faker.ancient().god();
        requestContract.setExternalId(externalId);

        // When
        client.post()
                .uri(getApiURI(USER_REST_API_POST_ME_ORGANIZATION))
                .body(BodyInserters.fromValue(requestContract))
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo(ORGANISATION_NOT_FOUND)
                .jsonPath("$.errors[0].message").isEqualTo("Organization not found for externalId " + externalId)
                .jsonPath("$.user").isEmpty();
    }


    private static RepositoryEntity buildRepository(final Integer vcsId,
                                                    final VcsOrganizationEntity vcsOrganizationEntity) {
        return RepositoryEntity.builder()
                .id("repo-" + vcsId)
                .name("repo-" + vcsId)
                .vcsOrganizationName(vcsOrganizationEntity.getName())
                .organizationId(vcsOrganizationEntity.getOrganizationEntity().getId())
                .build();
    }
}
