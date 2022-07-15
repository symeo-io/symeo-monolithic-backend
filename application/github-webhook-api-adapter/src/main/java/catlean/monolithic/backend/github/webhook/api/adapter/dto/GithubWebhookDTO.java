package catlean.monolithic.backend.github.webhook.api.adapter.dto;

import catlean.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubWebhookDTO {
    String action;
    GithubInstallationDTO installation;
}
