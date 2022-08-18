package io.symeo.monolithic.backend.github.webhook.api.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubAccountDTO {
    String login;
    Long id;
}
