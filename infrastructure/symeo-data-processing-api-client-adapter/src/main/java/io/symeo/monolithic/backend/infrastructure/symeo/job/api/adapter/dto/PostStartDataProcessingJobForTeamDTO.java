package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class PostStartDataProcessingJobForTeamDTO {
    @JsonProperty("organization_id")
    UUID organizationId;
    @JsonProperty("deploy_detection_type")
    String deployDetectionType;
    @JsonProperty("pull_request_merged_on_branch_regex")
    String pullRequestMergedOnBranchRegex;
    @JsonProperty("tag_regex")
    String tagRegex;
    @JsonProperty("exclude_branch_regexes")
    List<String> excludeBranchRegexes;
    @JsonProperty("team_id")
    UUID teamId;
    @JsonProperty("repository_ids")
    List<String> repositoryIds;
}
