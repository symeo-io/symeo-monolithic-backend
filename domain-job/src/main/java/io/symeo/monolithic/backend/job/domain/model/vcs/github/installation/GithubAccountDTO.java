package io.symeo.monolithic.backend.job.domain.model.vcs.github.installation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAccountDTO {

    String login;
    Long id;
}
