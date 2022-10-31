package io.symeo.monolithic.backend.job.domain.github.dto.installation;

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
