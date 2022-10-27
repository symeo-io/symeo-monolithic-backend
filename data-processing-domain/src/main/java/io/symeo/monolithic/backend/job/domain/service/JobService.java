package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.JobManager;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectVcsDataForRepositoriesAndDatesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.in.JobAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.JobExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.JobStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.VCS_ORGANIZATION_NOT_FOUND;

@AllArgsConstructor
@Slf4j
public class JobService implements JobAdapter {

    private final JobExpositionStorageAdapter jobExpositionStorageAdapter;
    private final JobStorage jobStorage;
    private final VcsDataProcessingService vcsDataProcessingService;
    private final JobManager jobManager;

    @Override
    public void startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                                Long vcsOrganizationId) throws SymeoException {
        final Optional<VcsOrganization> vcsOrganizationByIdAndOrganizationId =
                jobExpositionStorageAdapter.findVcsOrganizationByIdAndOrganizationId(vcsOrganizationId, organizationId);
        final VcsOrganization vcsOrganization = validateVcsOrganization(organizationId,
                vcsOrganizationId,
                vcsOrganizationByIdAndOrganizationId);
        jobManager.start(buildCollectRepositoriesJob(organizationId, vcsOrganization));
    }

    @Override
    public void startToCollectVcsDataForOrganizationIdAndRepositoryIds(UUID organizationId,
                                                                       List<String> repositoryIds) throws SymeoException {
        final List<Repository> allRepositoriesByIds =
                jobExpositionStorageAdapter.findAllRepositoriesByIds(repositoryIds);
        validateRepositories(organizationId, repositoryIds, allRepositoriesByIds);
        jobManager.start(buildCollectVcsDataJob(organizationId, allRepositoriesByIds));
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

    @Override
    public void startAll() throws SymeoException {

    }

    private Job buildCollectVcsDataJob(UUID organizationId, List<Repository> allRepositoriesByIds) {
        return Job.builder()
                .organizationId(organizationId)
                .jobRunnable(
                        CollectVcsDataForRepositoriesAndDatesJobRunnable.builder()
                                .repositories(allRepositoriesByIds)
                                .jobStorage(jobStorage)
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
                                .jobStorage(jobStorage)
                                .vcsOrganization(vcsOrganization)
                                .vcsDataProcessingService(vcsDataProcessingService)
                                .build()
                )
                .build();
    }
}