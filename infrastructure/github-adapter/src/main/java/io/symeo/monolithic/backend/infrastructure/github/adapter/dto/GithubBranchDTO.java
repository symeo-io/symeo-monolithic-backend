package io.symeo.monolithic.backend.infrastructure.github.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubBranchDTO {
    String name;
}
