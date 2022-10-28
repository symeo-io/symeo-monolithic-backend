package io.symeo.monolithic.backend.domain.bff.model.account.settings;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DeployDetectionSettings {

    String pullRequestMergedOnBranchRegex;
    String tagRegex;
    DeployDetectionTypeDomainEnum deployDetectionType;
    @Builder.Default
    List<String> excludeBranchRegexes = List.of();
}
