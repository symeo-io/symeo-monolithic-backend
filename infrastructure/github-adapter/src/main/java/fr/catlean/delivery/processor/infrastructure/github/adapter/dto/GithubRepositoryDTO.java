package fr.catlean.delivery.processor.infrastructure.github.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepositoryDTO {
  Long id;

  @JsonProperty("node_id")
  String nodeId;

  String name;

  @JsonProperty("full_name")
  String fullName;

  @JsonProperty("private")
  Boolean isPrivate;
}
