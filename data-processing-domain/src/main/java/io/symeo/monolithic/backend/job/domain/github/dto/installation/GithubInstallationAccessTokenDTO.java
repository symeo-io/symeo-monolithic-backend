package io.symeo.monolithic.backend.job.domain.github.dto.installation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubInstallationAccessTokenDTO {

    String token;
}
