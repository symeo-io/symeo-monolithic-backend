package io.symeo.monolithic.backend.domain.model.account.settings;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class OrganizationSettings {
    UUID id;
    UUID organizationId;
    DeliverySettings deliverySettings;

    public static OrganizationSettings initializeFromOrganizationIdAndDefaultBranch(final UUID organizationId,
                                                                                    final String defaultMostUsedBranch) {
        return OrganizationSettings.builder()
                .organizationId(organizationId)
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
