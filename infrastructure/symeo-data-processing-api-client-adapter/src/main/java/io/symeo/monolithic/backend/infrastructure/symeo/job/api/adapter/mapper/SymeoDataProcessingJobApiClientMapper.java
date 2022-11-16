package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.mapper;

import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForOrganizationDTO;
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


}
