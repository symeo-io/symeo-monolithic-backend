package io.symeo.monolithic.backend.job.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface DataProcessingJobAdapter {
    void startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                         Long vcsOrganizationId) throws SymeoException;

    void startToCollectVcsDataForOrganizationIdAndRepositoryIds(UUID organizationId, List<String> repositoryIds,
                                                                String deployDetectionType,
                                                                String pullRequestMergedOnBranchRegexes,
                                                                String tagRegex,
                                                                List<String> excludeBranchRegexes) throws SymeoException;

    void startToCollectVcsDataForOrganizationIdAndTeamIdAndRepositoryIds(UUID organizationId, UUID teamId,
                                                                         List<String> repositoryIds,
                                                                         String deployDetectionType,
                                                                         String pullRequestMergedOnBranchRegexes,
                                                                         String tagRegex,
                                                                         List<String> excludeBranchRegexes) throws SymeoException;

    void startToUpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettings(UUID organizationId,
                                                                                            List<String> repositoryIds,
                                                                                            String deployDetectionType,
                                                                                            String pullRequestMergedOnBranchRegex,
                                                                                            String tagRegex,
                                                                                            List<String> excludeBranchRegexes) throws SymeoException;
}
