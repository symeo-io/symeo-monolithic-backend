package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class PostStartDataProcessingJobForOrganizationDTO {
    @JsonProperty("organization_id")
    UUID organizationId;
    @JsonProperty("deploy_detection_type")
    String deployDetectionType;
    @JsonProperty("vcs_organization_id")
    Long vcsOrganizationId;
}
