package io.symeo.monolithic.backend.domain.bff.model.account.settings;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DeployDetectionSettings {

    @Builder.Default
    String pullRequestMergedOnBranchRegex = "^main$";
    String tagRegex;
    @Builder.Default
    DeployDetectionTypeDomainEnum deployDetectionType = DeployDetectionTypeDomainEnum.PULL_REQUEST;
    @Builder.Default
    List<String> excludeBranchRegexes = List.of();
}
