package io.symeo.monolithic.backend.domain.model.account.settings;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DeployDetectionSettings {

    String pullRequestMergedOnBranchRegex;
    String tagRegex;
    @Builder.Default
    List<String> excludeBranchRegexes = List.of();
}
