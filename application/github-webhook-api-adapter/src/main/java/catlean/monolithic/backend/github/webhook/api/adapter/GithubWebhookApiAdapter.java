package catlean.monolithic.backend.github.webhook.api.adapter;

import catlean.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookEventDTO;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import catlean.monolithic.backend.github.webhook.api.adapter.security.GithubWebhookSecretValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
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
        } catch (CatleanException | IOException e) {
            LOGGER.error("Error while consuming github webhook", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    private void handleGithubEvent(GithubWebhookEventDTO githubWebhookEventDTO, String githubEventType) throws CatleanException {
        if (githubWebhookEventDTO.getAction().equals("created") && githubEventType.equals("installation")) {
            organizationFacadeAdapter.createOrganizationForVcsNameAndExternalId(githubWebhookEventDTO.getInstallation().getAccount().getLogin(),
                    githubWebhookEventDTO.getInstallation().getId());
        } else {
            LOGGER.error("Invalid Github webhook {} for eventType {}", githubWebhookEventDTO, githubEventType);
        }
    }


}
