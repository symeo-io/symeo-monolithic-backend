package io.symeo.monolithic.backend.job.domain.model.organization;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrganizationSettingsView {
    String tagRegex;
    String pullRequestMergedOnBranchRegex;
    List<String> excludeBranchRegexes;
    String deployDetectionType;

}
