package catlean.monolithic.backend.github.webhook.api.adapter.it;

import catlean.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubWebhookApiAdapterIT extends AbstractGithubWebhookAdapterIT {


    @Value("${server.port}")
    private int serverPort;
    @Autowired
    public GithubWebhookProperties githubWebhookProperties;
    @Autowired
    public GithubWebhookITConfiguration.OrganizationAdapterMock organizationAdapterMock;

    @AfterEach
    void tearDown() {
        organizationAdapterMock.getOrganizations().clear();
    }

    @Test
    void should_invalidate_github_webhook_event_for_wrong_signature() throws IOException {
        // Given
        final byte[] bytes = Files.readString(Paths.get("target/test-classes/webhook_events" +
                "/post_installation_created_1.json")).getBytes();

        // When
        client.post()
                .uri("http://localhost:" + serverPort + "/github-app/webhook")
                .bodyValue(bytes)
                .header(
                        "X-GitHub-Event", "created"
                )
                .header("X-Hub-Signature-256", "signature")
                .exchange()
                // Then
                .expectStatus().is5xxServerError();
        assertThat(organizationAdapterMock.getOrganizations()).hasSize(0);
    }

    @Test
    void should_validate_and_consume_github_webhook_event() throws IOException {
        // Given
        final byte[] bytes = Files.readString(Paths.get("target/test-classes/webhook_events" +
                "/post_installation_created_1.json")).getBytes();

        // When
        client.post()
                .uri("http://localhost:" + serverPort + "/github-app/webhook")
                .bodyValue(bytes)
                .header(
                        "X-GitHub-Event", "installation"
                )
                .header("X-Hub-Signature-256", "sha256=" + new HmacUtils("HmacSHA256",
                        githubWebhookProperties.getSecret()).hmacHex(bytes))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful();
        assertThat(organizationAdapterMock.getOrganizations()).hasSize(1);
        final GithubWebhookEventDTO githubWebhookEventDTO = objectMapper.readValue(bytes, GithubWebhookEventDTO.class);
        assertThat(organizationAdapterMock.getOrganizations().get(0).getName()).isEqualTo(githubWebhookEventDTO.getInstallation().getAccount().getLogin());
        assertThat(organizationAdapterMock.getOrganizations().get(0).getExternalId()).isEqualTo(githubWebhookEventDTO.getInstallation().getId());
    }

    @Test
    void should_validate_and_not_consume_github_webhook_event() throws IOException {
        // Given
        final byte[] bytes = Files.readString(Paths.get("target/test-classes/webhook_events" +
                "/post_installation_created_1.json")).getBytes();

        // When
        client.post()
                .uri("http://localhost:" + serverPort + "/github-app/webhook")
                .bodyValue(bytes)
                .header(
                        "X-GitHub-Event", "fake-event-type"
                )
                .header("X-Hub-Signature-256", "sha256=" + new HmacUtils("HmacSHA256",
                        githubWebhookProperties.getSecret()).hmacHex(bytes))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful();
        assertThat(organizationAdapterMock.getOrganizations()).hasSize(0);
    }


}
