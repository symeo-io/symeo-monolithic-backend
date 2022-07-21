package fr.catlean.monolithic.backend.bootstrap.it;

import catlean.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.frontend.contract.api.model.LinkOrganizationToCurrentUserRequestContract;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
public class CatleanUserOnboardingIT extends AbstractCatleanMonolithicBackendIT {

    @Autowired
    public VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    public GithubWebhookProperties githubWebhookProperties;
    @Autowired
    public ObjectMapper objectMapper;

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


}
