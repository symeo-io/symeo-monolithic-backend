package io.symeo.monolithic.backend.job.domain.model.vcs.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubBranchDTO {
    String name;
}
