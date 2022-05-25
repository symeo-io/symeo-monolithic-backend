package fr.catlean.delivery.processor.infrastructure.github.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubOwnerDTO {

    String login;
}
