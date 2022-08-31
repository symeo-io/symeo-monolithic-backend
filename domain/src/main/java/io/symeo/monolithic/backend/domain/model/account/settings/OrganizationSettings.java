package io.symeo.monolithic.backend.domain.model.account.settings;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class OrganizationSettings {

    DeliverySettings deliverySettings;

    public static OrganizationSettings initializeFromDefaultBranch(final String defaultMostUsedBranch) {
        return OrganizationSettings.builder()
                .deliverySettings(
                        DeliverySettings.builder()
                                .deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .pullRequestMergedOnBranchRegex(String.format("^%s$",
                                                        defaultMostUsedBranch))
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
