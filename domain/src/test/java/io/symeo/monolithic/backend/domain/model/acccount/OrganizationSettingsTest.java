package io.symeo.monolithic.backend.domain.model.acccount;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationSettingsTest {

    private final Faker faker = new Faker();

    @Test
    void should_build_organization_settings_with_deploy_detection_from_default_most_used_branch() {
        // Given
        final String defaultMostUsedBranch = faker.rickAndMorty().character();
        final UUID organizationId = UUID.randomUUID();

        // When
        final OrganizationSettings organizationSettings =
                OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organizationId,
                        defaultMostUsedBranch);

        // Then
        assertThat(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex())
                .isEqualTo(String.format("^%s$", defaultMostUsedBranch));
        assertThat(organizationSettings.getOrganizationId()).isEqualTo(organizationId);
    }
}
