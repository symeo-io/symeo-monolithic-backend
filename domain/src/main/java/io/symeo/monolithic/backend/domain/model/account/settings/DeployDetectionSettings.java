package io.symeo.monolithic.backend.domain.model.account.settings;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeployDetectionSettings {

    String pullRequestMergedOnBranchRegex;
    String tagRegex;
}
