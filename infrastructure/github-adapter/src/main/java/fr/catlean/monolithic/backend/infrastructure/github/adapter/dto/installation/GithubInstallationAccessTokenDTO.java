package fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.installation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubInstallationAccessTokenDTO {

    String token;
}
