package fr.catlean.monolithic.backend.bootstrap.it;

import catlean.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.LinkOrganizationToCurrentUserRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.UpdateOnboardingRequestContract;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.job.JobRepository;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.ORGANISATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@TestMethodOrder(OrderAnnotation.class)
public class CatleanUserOnboardingIT extends AbstractCatleanMonolithicBackendIT {

    @Autowired
    public VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    public GithubWebhookProperties githubWebhookProperties;
    @Autowired
    public ObjectMapper objectMapper;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public JobRepository jobRepository;

    private static final String mail = faker.name().firstName() + "@" + faker.name().firstName() + ".fr";


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

    @Order(3)
    @Test
    void should_linked_organization_to_user() {
        // Given
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
                .jsonPath("$.user.organization.id").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId())
                .jsonPath("$.user.organization.name").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getName());
    }

    @Order(4)
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
                .jsonPath("$.user.organization.id").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId())
                .jsonPath("$.user.organization.name").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getName());
    }


    @Order(5)
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


    @Order(6)
    @Test
    void should_configure_two_teams() {
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
                .jsonPath("$.teams[0].id").isEqualTo(teams.get(0).getId())
                .jsonPath("$.teams[0].name").isEqualTo(teams.get(0).getName())
                .jsonPath("$.teams[0].repository_ids[0]").isEqualTo(teams.get(0).getRepositoryIds().get(0))
                .jsonPath("$.teams[0].repository_ids[1]").isEqualTo(teams.get(0).getRepositoryIds().get(1))
                .jsonPath("$.teams[0].name").isNotEmpty()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.teams[1].id").isEqualTo(teams.get(1).getId())
                .jsonPath("$.teams[1].name").isEqualTo(teams.get(1).getName())
                .jsonPath("$.teams[1].repository_ids[0]").isEqualTo(teams.get(1).getRepositoryIds().get(0))
                .jsonPath("$.teams[1].repository_ids[1]").isEqualTo(teams.get(1).getRepositoryIds().get(1))
                .jsonPath("$.teams[1].name").isNotEmpty();

    }

    @Order(7)
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
                .jsonPath("$.teams[0].id").isEqualTo(teams.get(0).getId())
                .jsonPath("$.teams[0].name").isEqualTo(teams.get(0).getName())
                .jsonPath("$.teams[0].repository_ids[0]").isEqualTo(teams.get(0).getRepositoryIds().get(0))
                .jsonPath("$.teams[0].repository_ids[1]").isEqualTo(teams.get(0).getRepositoryIds().get(1))
                .jsonPath("$.teams[0].name").isNotEmpty()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.teams[1].id").isEqualTo(teams.get(1).getId())
                .jsonPath("$.teams[1].name").isEqualTo(teams.get(1).getName())
                .jsonPath("$.teams[1].repository_ids[0]").isEqualTo(teams.get(1).getRepositoryIds().get(0))
                .jsonPath("$.teams[1].repository_ids[1]").isEqualTo(teams.get(1).getRepositoryIds().get(1))
                .jsonPath("$.teams[1].name").isNotEmpty();

    }

    @Order(8)
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
                .jsonPath("$.user.organization.id").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId())
                .jsonPath("$.user.organization.name").isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getName());
    }


    @Order(9)
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

    @Order(10)
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

    @Order(11)
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
                .is5xxServerError()
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
