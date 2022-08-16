package fr.catlean.monolithic.backend.github.webhook.api.adapter.properties;

import lombok.Data;

@Data
public class GithubWebhookProperties {
    private String secret;
}
