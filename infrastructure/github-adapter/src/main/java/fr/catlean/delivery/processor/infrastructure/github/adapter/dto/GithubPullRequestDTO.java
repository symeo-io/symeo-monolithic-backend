package fr.catlean.delivery.processor.infrastructure.github.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubPullRequestDTO {

    Integer id;
    Integer number;
    String state;
    String title;
    @JsonProperty("created_at")
    Date creationDate;
    @JsonProperty("updated_at")
    Date updateDate;
    @JsonProperty("closed_at")
    Date closeDate;
    @JsonProperty("merged_at")
    Date mergeDate;
    Boolean draft;
}
