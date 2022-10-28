package io.symeo.monolithic.backend.infrastructure.postgres.mapper.account;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface OrganizationMapper {

    static OrganizationEntity domainToEntity(final Organization organization) {
        return OrganizationEntity.builder()
                .id(isNull(organization.getId()) ? UUID.randomUUID() : organization.getId())
                .name(organization.getName())
                .build();
    }

    static Organization entityToDomain(final OrganizationEntity organizationEntity) {
        return Organization.builder()
                .name(organizationEntity.getName())
                .id(organizationEntity.getId())
                .build();

    }


    static Organization entityToDomain(final VcsOrganizationEntity vcsOrganizationEntity) {
        final OrganizationEntity organizationEntity = vcsOrganizationEntity.getOrganizationEntity();
        return Organization.builder()
                .id(organizationEntity.getId())
                .name(organizationEntity.getName())
                .vcsOrganization(vcsEntityToDomain(vcsOrganizationEntity))
                .build();
    }

    static VcsOrganizationEntity vcsDomainToEntity(final Organization organization) {
        final Organization.VcsOrganization vcsOrganization = organization.getVcsOrganization();
        return VcsOrganizationEntity.builder()
                .organizationEntity(domainToEntity(organization))
                .vcsId(vcsOrganization.getVcsId())
                .name(vcsOrganization.getName())
                .externalId(vcsOrganization.getExternalId())
                .build();
    }

    static Organization.VcsOrganization vcsEntityToDomain(final VcsOrganizationEntity vcsOrganizationEntity) {
        return Organization.VcsOrganization.builder()
                .vcsId(vcsOrganizationEntity.getVcsId())
                .id(vcsOrganizationEntity.getId())
                .name(vcsOrganizationEntity.getName())
                .externalId(vcsOrganizationEntity.getExternalId())
                .build();
    }

    static VcsOrganization dataProcessingVcsEntityToDomain(final VcsOrganizationEntity vcsOrganizationEntity) {
        return VcsOrganization.builder()
                .vcsId(vcsOrganizationEntity.getVcsId())
                .id(vcsOrganizationEntity.getId())
                .organizationId(vcsOrganizationEntity.getOrganizationId())
                .name(vcsOrganizationEntity.getName())
                .externalId(vcsOrganizationEntity.getExternalId())
                .build();
    }

    static OrganizationSettingsEntity settingsToEntity(final OrganizationSettings organizationSettings) {
        final DeployDetectionSettings deployDetectionSettings =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings();
        return OrganizationSettingsEntity.builder()
                .id(isNull(organizationSettings.getId()) ? UUID.randomUUID() : organizationSettings.getId())
                .organizationId(organizationSettings.getOrganizationId())
                .tagRegex(deployDetectionSettings.getTagRegex())
                .pullRequestMergedOnBranchRegex(deployDetectionSettings.getPullRequestMergedOnBranchRegex())
                .excludeBranchRegexes(deployDetectionSettings.getExcludeBranchRegexes())
                .deployDetectionType(deployDetectionSettings.getDeployDetectionType().toString())
                .build();
    }

    static OrganizationSettings settingsToDomain(final OrganizationSettingsEntity organizationSettingsEntity) {
        return OrganizationSettings.builder()
                .id(organizationSettingsEntity.getId())
                .organizationId(organizationSettingsEntity.getOrganizationId())
                .deliverySettings(
                        DeliverySettings.builder()
                                .deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .tagRegex(organizationSettingsEntity.getTagRegex())
                                                .pullRequestMergedOnBranchRegex(organizationSettingsEntity.getPullRequestMergedOnBranchRegex())
                                                .excludeBranchRegexes(organizationSettingsEntity.getExcludeBranchRegexes())
                                                .deployDetectionType(DeployDetectionTypeDomainEnum.valueOf(organizationSettingsEntity.getDeployDetectionType()))
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
