package io.symeo.monolithic.backend.infrastructure.github.adapter.dto.installation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAccountDTO {

    String login;
    Long id;
}
