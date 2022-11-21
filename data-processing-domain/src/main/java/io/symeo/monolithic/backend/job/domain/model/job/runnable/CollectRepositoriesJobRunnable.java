package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.AbstractTasksRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.JobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.organization.OrganizationSettingsView;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Slf4j
public class CollectRepositoriesJobRunnable extends AbstractTasksRunnable<VcsOrganization> implements JobRunnable {

    public static final String JOB_CODE = "COLLECT_REPOSITORIES_FOR_VCS_ORGANIZATION_JOB";
    @NonNull
    private final VcsDataProcessingService vcsDataProcessingService;
    @NonNull
    private final DataProcessingJobStorage dataProcessingJobStorage;
    @NonNull
    private final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter;
    @NonNull
    private final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter;
    @NonNull
    private final VcsOrganization vcsOrganization;
    @NonNull
    private final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter;

    @Override
    public void run(Long jobId) throws SymeoException {
        executeAllTasks(this::collectRepositoriesForVcsOrganization, dataProcessingJobStorage, jobId);
    }

    private void collectRepositoriesForVcsOrganization(VcsOrganization vcsOrganization) throws SymeoException {
        vcsDataProcessingService.collectRepositoriesForVcsOrganization(vcsOrganization);
        final OrganizationSettingsView organizationSettingsView =
                vcsOrganizationStorageAdapter.findOrganizationSettingsViewForOrganizationId(vcsOrganization.getOrganizationId());
        final List<Repository> repositoriesLinkedToATeam =
                dataProcessingExpositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(vcsOrganization.getOrganizationId());
        if (repositoriesLinkedToATeam.size() > 0) {
            autoSymeoDataProcessingJobApiAdapter.autoStartDataProcessingJobForOrganizationIdAndRepositoryIds(
                    vcsOrganization.getOrganizationId(),
                    repositoriesLinkedToATeam.stream().map(Repository::getId).toList(),
                    organizationSettingsView.getDeployDetectionType(),
                    organizationSettingsView.getPullRequestMergedOnBranchRegex(),
                    organizationSettingsView.getTagRegex(), organizationSettingsView.getExcludeBranchRegexes()
            );
        }
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public void initializeTasks() {
        this.setTasks(new ArrayList<>(List.of(Task.newTaskForInput(vcsOrganization))));
    }
}
