package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.JobManager;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectVcsDataForRepositoriesAndDatesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.VCS_ORGANIZATION_NOT_FOUND;

@AllArgsConstructor
@Slf4j
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter;
    private final DataProcessingJobStorage dataProcessingJobStorage;
    private final VcsDataProcessingService vcsDataProcessingService;
    private final JobManager jobManager;
    private final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter;
    private final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter;

    @Override
    public void startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                                Long vcsOrganizationId) throws SymeoException {
        final Optional<VcsOrganization> vcsOrganizationByIdAndOrganizationId =
                dataProcessingExpositionStorageAdapter.findVcsOrganizationByIdAndOrganizationId(vcsOrganizationId,
                        organizationId);
        final VcsOrganization vcsOrganization = validateVcsOrganization(organizationId,
                vcsOrganizationId,
                vcsOrganizationByIdAndOrganizationId);
        jobManager.start(buildCollectRepositoriesJob(organizationId, vcsOrganization));
    }

    @Override
    public void startToCollectVcsDataForOrganizationIdAndTeamIdAndRepositoryIds(final UUID organizationId,
                                                                                final UUID teamId,
                                                                                final List<String> repositoryIds,
                                                                                final String deployDetectionType,
                                                                                final String pullRequestMergedOnBranchRegexes,
                                                                                final String tagRegex,
                                                                                final List<String> excludeBranchRegexes) throws SymeoException {
        final List<Repository> allRepositoriesByIds =
                dataProcessingExpositionStorageAdapter.findAllRepositoriesByIds(repositoryIds);
        validateRepositories(organizationId, repositoryIds, allRepositoriesByIds);
        jobManager.start(buildCollectVcsDataForTeamIdJob(
                organizationId, teamId, allRepositoriesByIds, deployDetectionType, pullRequestMergedOnBranchRegexes,
                tagRegex, excludeBranchRegexes)
        );
    }

    @Override
    public void startToCollectVcsDataForOrganizationIdAndRepositoryIds(final UUID organizationId,
                                                                       final List<String> repositoryIds,
                                                                       final String deployDetectionType,
                                                                       final String pullRequestMergedOnBranchRegexes,
                                                                       final String tagRegex,
                                                                       final List<String> excludeBranchRegexes) throws SymeoException {
        final List<Repository> allRepositoriesByIds =
                dataProcessingExpositionStorageAdapter.findAllRepositoriesByIds(repositoryIds);
        validateRepositories(organizationId, repositoryIds, allRepositoriesByIds);
        jobManager.start(buildCollectVcsDataJob(organizationId, allRepositoriesByIds, deployDetectionType,
                pullRequestMergedOnBranchRegexes,
                tagRegex, excludeBranchRegexes));
    }

    private static VcsOrganization validateVcsOrganization(UUID organizationId, Long vcsOrganizationId,
                                                           Optional<VcsOrganization> vcsOrganizationByIdAndOrganizationId) throws SymeoException {
        return vcsOrganizationByIdAndOrganizationId
                .orElseThrow(() -> {
                    final String message = String.format("VcsOrganization not found for vcsOrganizationId %s " +
                                    "and organizationId %s",
                            vcsOrganizationId, organizationId);
                    LOGGER.error(message);
                    return SymeoException.builder()
                            .code(VCS_ORGANIZATION_NOT_FOUND)
                            .message(message)
                            .build();
                });
    }

    private static void validateRepositories(UUID organizationId, List<String> repositoryIds,
                                             List<Repository> allRepositoriesByIds) throws SymeoException {
        if (allRepositoriesByIds.size() < repositoryIds.size()) {
            final List<String> allRepositoryIds = allRepositoriesByIds.stream().map(Repository::getId).toList();
            final List<String> repositoryIdsNotFound = repositoryIds.stream()
                    .filter(id -> !allRepositoryIds.contains(id))
                    .toList();
            final String message = String.format("Repositories %s not found for " +
                    "organizationId %s", repositoryIdsNotFound, organizationId);
            LOGGER.error(message);
            throw SymeoException.builder()
                    .message(message)
                    .code(SymeoExceptionCode.REPOSITORIES_NOT_FOUND)
                    .build();
        }
    }

    private Job buildCollectVcsDataJob(final UUID organizationId, final List<Repository> allRepositoriesByIds,
                                       final String deployDetectionType,
                                       final String pullRequestMergedOnBranchRegexes,
                                       final String tagRegex,
                                       final List<String> excludeBranchRegexes) {
        return Job.builder()
                .organizationId(organizationId)
                .jobRunnable(
                        CollectVcsDataForRepositoriesAndDatesJobRunnable.builder()
                                .repositories(allRepositoriesByIds)
                                .dataProcessingJobStorage(dataProcessingJobStorage)
                                .vcsDataProcessingService(vcsDataProcessingService)
                                .deployDetectionType(deployDetectionType)
                                .pullRequestMergedOnBranchRegexes(pullRequestMergedOnBranchRegexes)
                                .tagRegex(tagRegex)
                                .excludeBranchRegexes(excludeBranchRegexes)
                                .build()
                )
                .build();
    }

    private Job buildCollectVcsDataForTeamIdJob(final UUID organizationId, final UUID teamId,
                                                final List<Repository> allRepositoriesByIds,
                                                final String deployDetectionType,
                                                final String pullRequestMergedOnBranchRegexes,
                                                final String tagRegex,
                                                final List<String> excludeBranchRegexes) {
        return Job.builder()
                .organizationId(organizationId)
                .teamId(teamId)
                .jobRunnable(
                        CollectVcsDataForRepositoriesAndDatesJobRunnable.builder()
                                .repositories(allRepositoriesByIds)
                                .deployDetectionType(deployDetectionType)
                                .pullRequestMergedOnBranchRegexes(pullRequestMergedOnBranchRegexes)
                                .tagRegex(tagRegex)
                                .excludeBranchRegexes(excludeBranchRegexes)
                                .dataProcessingJobStorage(dataProcessingJobStorage)
                                .vcsDataProcessingService(vcsDataProcessingService)
                                .build()
                )
                .build();
    }


    private Job buildCollectRepositoriesJob(UUID organizationId, VcsOrganization vcsOrganization) {
        return Job.builder()
                .organizationId(organizationId)
                .jobRunnable(
                        CollectRepositoriesJobRunnable.builder()
                                .dataProcessingJobStorage(dataProcessingJobStorage)
                                .vcsOrganization(vcsOrganization)
                                .vcsDataProcessingService(vcsDataProcessingService)
                                .dataProcessingExpositionStorageAdapter(dataProcessingExpositionStorageAdapter)
                                .autoSymeoDataProcessingJobApiAdapter(autoSymeoDataProcessingJobApiAdapter)
                                .vcsOrganizationStorageAdapter(vcsOrganizationStorageAdapter)
                                .build()
                )
                .build();
    }
}
