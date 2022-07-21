package fr.catlean.monolithic.backend.bootstrap.it;

import catlean.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CatleanUserOnboardingIT extends AbstractCatleanMonolithicBackendIT {

    @Autowired
    public VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    public GithubWebhookProperties githubWebhookProperties;
    @Autowired
    public ObjectMapper objectMapper;

    @Order(1)
    @Test
    public void should_create_an_organization_triggered_by_a_github_webhook_installation_event() throws IOException {
        final byte[] githubWebhookInstallationCreatedEventBytes = Files.readString(Paths.get("target/test-classes" +
                "/webhook_events" +
                "/post_installation_created_1.json")).getBytes();
        final String hmacSHA256 = new HmacUtils("HmacSHA256",
                githubWebhookProperties.getSecret()).hmacHex(githubWebhookInstallationCreatedEventBytes);
        client
                .post()
                .uri(getApiURI(GITHUB_WEBHOOK_API))
                .bodyValue(githubWebhookInstallationCreatedEventBytes)
                .header("X-GitHub-Event", "installation")
                .header("X-Hub-Signature-256", "sha256=" + hmacSHA256)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        final List<VcsOrganizationEntity> vcsOrganizationEntities = vcsOrganizationRepository.findAll();
        assertThat(vcsOrganizationEntities).hasSize(1);
        final GithubWebhookEventDTO githubWebhookEventDTO =
                objectMapper.readValue(githubWebhookInstallationCreatedEventBytes, GithubWebhookEventDTO.class);
        assertThat(vcsOrganizationEntities.get(0).getName()).isEqualTo(githubWebhookEventDTO.getInstallation().getAccount().getLogin());
        assertThat(vcsOrganizationEntities.get(0).getVcsId()).isEqualTo("github-" + githubWebhookEventDTO.getInstallation().getAccount().getId());
        assertThat(vcsOrganizationEntities.get(0).getExternalId()).isEqualTo(githubWebhookEventDTO.getInstallation().getId());
        assertThat(vcsOrganizationEntities.get(0).getOrganizationEntity().getName()).isEqualTo(githubWebhookEventDTO.getInstallation().getAccount().getLogin());
    }
}
