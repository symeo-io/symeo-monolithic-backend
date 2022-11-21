package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface BffSymeoDataProcessingJobApiAdapter {

    void startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(UUID organizationId, UUID teamId,
                                                                          List<String> repositoryIds,
                                                                          String deployDetectionType,
                                                                          String pullRequestMergedOnBranchRegex,
                                                                          String tagRegex,
                                                                          List<String> excludeBranchRegexes) throws SymeoException;

    void startDataProcessingJobForOrganizationIdAndVcsOrganizationId(UUID organizationId, Long vcsOrganizationId) throws SymeoException;

    void startUpdateCycleTimesDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettings(List<String> repositoryIds,
                                                                                                        OrganizationSettings organizationSettings) throws SymeoException;
}
