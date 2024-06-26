package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.port.out.BffSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.mapper.SymeoDataProcessingJobApiClientMapper.*;

@AllArgsConstructor
public class SymeoDataProcessingJobApiClientAdapter implements BffSymeoDataProcessingJobApiAdapter,
        AutoSymeoDataProcessingJobApiAdapter {

    private final SymeoHttpClient symeoHttpClient;

    @Override
    public void autoStartDataProcessingJobForOrganizationIdAndRepositoryIds(UUID organizationId,
                                                                            List<String> repositoryIdsLinkedToATeam,
                                                                            String deployDetectionType,
                                                                            String pullRequestMergedOnBranchRegex,
                                                                            String tagRegex,
                                                                            List<String> excludeBranchRegexes) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndRepositoryIds(
                domainToRepositoriesDTO(organizationId, repositoryIdsLinkedToATeam, deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludeBranchRegexes));
    }

    @Override
    public void startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(UUID organizationId, UUID teamId,
                                                                                 List<String> repositoryIds,
                                                                                 String deployDetectionType,
                                                                                 String pullRequestMergedOnBranchRegex,
                                                                                 String tagRegex,
                                                                                 List<String> excludeBranchRegexes) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(
                domainToTeamDTO(organizationId, teamId, repositoryIds, deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludeBranchRegexes));
    }

    @Override
    public void startDataProcessingJobForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                            Long vcsOrganizationId) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndVcsOrganizationId(domainToVcsOrganizationDTO(organizationId, vcsOrganizationId));
    }

    @Override
    public void startUpdateCycleTimesDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettings(List<String> repositoryIds,
                                                                                                               OrganizationSettings organizationSettings)
            throws SymeoException {
        symeoHttpClient.startUpdateCycleTimesDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettings(
                domainToOrganizationSettingsDTO(repositoryIds, organizationSettings));
    }

    @Override
    public void autoStartDataProcessingJobForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                                Long vcsOrganizationId) throws SymeoException {
        symeoHttpClient.startDataProcessingJobForOrganizationIdAndVcsOrganizationId(domainToVcsOrganizationDTO(organizationId, vcsOrganizationId));
    }


}
