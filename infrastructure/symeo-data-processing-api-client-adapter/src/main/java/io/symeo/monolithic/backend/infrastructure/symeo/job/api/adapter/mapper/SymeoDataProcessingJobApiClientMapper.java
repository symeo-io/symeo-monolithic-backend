package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForOrganizationDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForRepositoriesDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForTeamDTO;

import java.util.List;
import java.util.UUID;

public interface SymeoDataProcessingJobApiClientMapper {

    static PostStartDataProcessingJobForOrganizationDTO domainToVcsOrganizationDTO(final UUID organizationId,
                                                                                   final Long vcsOrganizationId) {
        return PostStartDataProcessingJobForOrganizationDTO.builder()
                .organizationId(organizationId)
                .vcsOrganizationId(vcsOrganizationId)
                .build();
    }

    static PostStartDataProcessingJobForTeamDTO domainToTeamDTO(final UUID organizationId,
                                                                final UUID teamId,
                                                                final List<String> repositoryIds,
                                                                final String deployDetectionType,
                                                                final String pullRequestMergedOnBranchRegex,
                                                                final String tagRegex,
                                                                final List<String> excludeBranchRegexes) {
        return PostStartDataProcessingJobForTeamDTO.builder()
                .organizationId(organizationId)
                .teamId(teamId)
                .repositoryIds(repositoryIds)
                .deployDetectionType(deployDetectionType)
                .pullRequestMergedOnBranchRegex(pullRequestMergedOnBranchRegex)
                .tagRegex(tagRegex)
                .excludeBranchRegexes(excludeBranchRegexes)
                .build();
    }

    static PostStartDataProcessingJobForRepositoriesDTO domainToRepositoriesDTO(final UUID organizationId,
                                                                                final List<String> repositoryIds,
                                                                                final String deployDetectionType,
                                                                                final String pullRequestMergedOnBranchRegex,
                                                                                final String tagRegex,
                                                                                final List<String> excludeBranchRegexes) {
        return PostStartDataProcessingJobForRepositoriesDTO.builder()
                .organizationId(organizationId)
                .repositoryIds(repositoryIds)
                .deployDetectionType(deployDetectionType)
                .pullRequestMergedOnBranchRegex(pullRequestMergedOnBranchRegex)
                .tagRegex(tagRegex)
                .excludeBranchRegexes(excludeBranchRegexes)
                .build();
    }


    static PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsDTO domainToOrganizationSettingsDTO(List<String> repositoryIds,
                                                                                                                                 final OrganizationSettings organizationSettings) {

        return PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsDTO.builder()
                .organizationId(organizationSettings.getOrganizationId())
                .repositoryIds(repositoryIds)
                .deployDetectionType(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType().getValue())
                .pullRequestMergedOnBranchRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex())
                .tagRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex())
                .excludeBranchRegexes(organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes())
                .build();
    }
}
