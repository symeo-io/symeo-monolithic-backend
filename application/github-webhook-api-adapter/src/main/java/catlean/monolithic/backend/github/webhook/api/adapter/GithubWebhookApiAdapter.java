package catlean.monolithic.backend.github.webhook.api.adapter;

import catlean.monolithic.backend.github.webhook.api.adapter.dto.GithubWebhookDTO;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class GithubWebhookApiAdapter {

    private static final String X_GITHUB_EVENT = "X-GitHub-Event";
    private final OrganizationFacadeAdapter organizationFacadeAdapter;

    @PostMapping("/github-app/webhook")
    public ResponseEntity<Void> consumeWebhook(final @RequestBody GithubWebhookDTO githubWebhookDTO,
                                               final @RequestHeader(X_GITHUB_EVENT) String githubEventType) {
        LOGGER.info("EventType = {} and Event = {}", githubEventType, githubWebhookDTO);
        try {
            handleGithubEvent(githubWebhookDTO, githubEventType);
            return ResponseEntity.ok().build();
        } catch (CatleanException e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    private void handleGithubEvent(GithubWebhookDTO githubWebhookDTO, String githubEventType) throws CatleanException {
        if (githubWebhookDTO.getAction().equals("created") && githubEventType.equals("installation")) {
            organizationFacadeAdapter.createOrganizationForVcsNameAndExternalId(githubWebhookDTO.getInstallation().getAccount().getLogin(),
                    githubWebhookDTO.getInstallation().getId());

        } else {
            LOGGER.error("Invalid Github webhook {} for eventType {}", githubWebhookDTO, githubEventType);
        }
    }


}