package io.symeo.monolithic.backend.domain.bff.model.account.settings;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Builder
@Value
public class OrganizationSettings {
    UUID id;
    UUID organizationId;
    DeliverySettings deliverySettings;

    public static OrganizationSettings initializeFromOrganizationId(final UUID organizationId) {
        return OrganizationSettings.builder()
                .organizationId(organizationId)
                .deliverySettings(
                        DeliverySettings.builder()
                                .deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .pullRequestMergedOnBranchRegex("^main$")
                                                .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                                .deployDetectionType(DeployDetectionTypeDomainEnum.PULL_REQUEST)
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
