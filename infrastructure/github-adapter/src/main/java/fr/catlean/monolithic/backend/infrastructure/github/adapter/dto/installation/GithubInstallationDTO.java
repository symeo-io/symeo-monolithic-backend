package fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.installation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubInstallationDTO {

    Long id;
    GithubAccountDTO account;
    @JsonProperty("access_tokens_url")
    String accessTokensUrl;
}
