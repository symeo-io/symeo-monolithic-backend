package catlean.monolithic.backend.github.webhook.api.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubInstallationDTO {
    String id;
    GithubAccountDTO account;
}
