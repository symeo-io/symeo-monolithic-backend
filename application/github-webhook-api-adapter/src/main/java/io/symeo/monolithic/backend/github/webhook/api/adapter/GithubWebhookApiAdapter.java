package io.symeo.monolithic.backend.github.webhook.api.adapter;

import io.symeo.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.security.GithubWebhookSecretValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@AllArgsConstructor
public class GithubWebhookApiAdapter {

    private static final String X_GITHUB_EVENT = "X-GitHub-Event";
    private static final String X_HUB_SIGNATURE_256 = "X-Hub-Signature-256";
    private final OrganizationFacadeAdapter organizationFacadeAdapter;
    private final GithubWebhookProperties githubWebhookProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/github-app/webhook")
    public ResponseEntity<Void> consumeWebhook(final @RequestBody byte[] githubWebhookDTOBytes,
                                               final @RequestHeader(X_GITHUB_EVENT) String githubEventType,
                                               final @RequestHeader(X_HUB_SIGNATURE_256) String githubSha256Signature) {


        try {
            GithubWebhookSecretValidator.validateWebhook(githubWebhookDTOBytes, githubWebhookProperties.getSecret(),
                    githubSha256Signature);
            final GithubWebhookEventDTO githubWebhookEventDTO = objectMapper.readValue(githubWebhookDTOBytes,
                    GithubWebhookEventDTO.class);
            LOGGER.info("EventType = {} and Event = {}", githubEventType, githubWebhookEventDTO);
            handleGithubEvent(githubWebhookEventDTO, githubEventType);
            return ResponseEntity.ok().build();
        } catch (SymeoException | IOException e) {
            LOGGER.error("Error while consuming github webhook", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    private void handleGithubEvent(GithubWebhookEventDTO githubWebhookEventDTO, String githubEventType) throws SymeoException {
        if (githubWebhookEventDTO.getAction().equals("created") && githubEventType.equals("installation")) {
            final String organizationName = githubWebhookEventDTO.getInstallation().getAccount().getLogin();
            organizationFacadeAdapter.createOrganization(Organization.builder()
                    .name(organizationName)
                    .vcsOrganization(
                            VcsOrganization.builder()
                                    .externalId(githubWebhookEventDTO.getInstallation().getId())
                                    .name(organizationName)
                                    .vcsId("github-" + githubWebhookEventDTO.getInstallation().getAccount().getId())
                                    .build()
                    )
                    .build()
            );
        } else {
            LOGGER.error("Invalid Github webhook {} for eventType {}", githubWebhookEventDTO, githubEventType);
        }
    }


}
