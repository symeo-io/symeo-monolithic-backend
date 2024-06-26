package io.symeo.monolithic.backend.job.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.data.processing.contract.api.DataProcessingJobApi;
import io.symeo.monolithic.backend.data.processing.contract.api.model.*;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.function.SymeoRunnable;
import io.symeo.monolithic.backend.job.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.job.domain.port.in.OrganizationJobFacade;
import io.symeo.monolithic.backend.job.rest.api.adapter.mapper.SymeoErrorContractMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "DataProcessingJob"))
@Slf4j
public class DataProcessingRestApiAdapter implements DataProcessingJobApi {

    private final DataProcessingJobAdapter dataProcessingJobAdapter;
    private final OrganizationJobFacade organizationJobFacade;
    private final String jobApiKey;
    private final String jobApiHeaderKey;

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startDataProcessingJobForOrganizationIdAndRepositoryIds(final String X_SYMEO_JOB_KEY_X,
                                                                                                                     final PostStartDataProcessingJobForOrganizationContract postStartDataProcessingJobForOrganizationContract) {
        final UUID organizationId = postStartDataProcessingJobForOrganizationContract.getOrganizationId();
        final List<String> repositoryIds = postStartDataProcessingJobForOrganizationContract.getRepositoryIds();
        final String deployDetectionType = postStartDataProcessingJobForOrganizationContract.getDeployDetectionType();
        final String pullRequestMergedOnBranchRegex = postStartDataProcessingJobForOrganizationContract.getPullRequestMergedOnBranchRegex();
        final String tagRegex = postStartDataProcessingJobForOrganizationContract.getTagRegex();
        final List<String> excludeBranchRegexes = postStartDataProcessingJobForOrganizationContract.getExcludeBranchRegexes();
        SymeoRunnable jobToStart =
                () -> dataProcessingJobAdapter.startToCollectVcsDataForOrganizationIdAndRepositoryIds(
                        organizationId,
                        repositoryIds,
                        deployDetectionType,
                        pullRequestMergedOnBranchRegex,
                        tagRegex,
                        excludeBranchRegexes
                );
        final String errorMessage = String.format("Error while starting data processing jobs for organizationId %s " +
                "and repositoryIds %s", organizationId, repositoryIds);
        return runJobAndReturnResponseEntity(X_SYMEO_JOB_KEY_X, jobToStart, errorMessage);
    }

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(final String X_SYMEO_JOB_KEY_X,
                                                                                                                              final PostStartDataProcessingJobForTeamContract postStartDataProcessingJobForTeamContract) {
        final UUID organizationId = postStartDataProcessingJobForTeamContract.getOrganizationId();
        final UUID teamId = postStartDataProcessingJobForTeamContract.getTeamId();
        final List<String> repositoryIds = postStartDataProcessingJobForTeamContract.getRepositoryIds();
        final String deployDetectionType = postStartDataProcessingJobForTeamContract.getDeployDetectionType();
        final String pullRequestMergedOnBranchRegex = postStartDataProcessingJobForTeamContract.getPullRequestMergedOnBranchRegex();
        final String tagRegex = postStartDataProcessingJobForTeamContract.getTagRegex();
        final List<String> excludeBranchRegexes = postStartDataProcessingJobForTeamContract.getExcludeBranchRegexes();
        SymeoRunnable jobToStart =
                () -> dataProcessingJobAdapter.startToCollectVcsDataForOrganizationIdAndTeamIdAndRepositoryIds(
                        organizationId,
                        teamId,
                        repositoryIds,
                        deployDetectionType,
                        pullRequestMergedOnBranchRegex,
                        tagRegex,
                        excludeBranchRegexes
                );
        final String errorMessage = String.format("Error while starting data processing jobs for organizationId %s " +
                "and teamId %s and repositoryIds %s", organizationId, teamId, repositoryIds);
        return runJobAndReturnResponseEntity(X_SYMEO_JOB_KEY_X, jobToStart, errorMessage);
    }

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startUpdateCycleTimesDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettings(String X_SYMEO_JOB_KEY_X, PostStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract) {

        final UUID organizationId = postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.getOrganizationId();
        final List<String> repositoryIds = postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.getRepositoryIds();
        final String deployDetectionType = postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.getDeployDetectionType();
        final String pullRequestMergedOnBranchRegex = postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.getPullRequestMergedOnBranchRegex();
        final String tagRegex = postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.getTagRegex();
        final List<String> excludeBranchRegexes = postStartDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettingsContract.getExcludeBranchRegexes();
        SymeoRunnable jobToStart =
                () -> dataProcessingJobAdapter.startToUpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettings(
                        organizationId,
                        repositoryIds,
                        deployDetectionType,
                        pullRequestMergedOnBranchRegex,
                        tagRegex,
                        excludeBranchRegexes
                );
        final String errorMessage = String.format("Error while starting cycle times update jobs for organizationId %s " +
                "and repositoryIds %s and deployDetectionType %s and pullRequestMergedOnBranchRegex %s and tagRegex %s and excludeBranchRegexes %s",
                organizationId, repositoryIds, deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludeBranchRegexes);
        return runJobAndReturnResponseEntity(X_SYMEO_JOB_KEY_X, jobToStart, errorMessage);
    }

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startDataProcessingJobForOrganizationIdAndVcsOrganizationId(final String X_SYMEO_JOB_KEY_X,
                                                                                                                         final PostStartDataProcessingJobForVcsOrganizationContract postStartDataProcessingJobForVcsOrganizationContract) {
        final UUID organizationId = postStartDataProcessingJobForVcsOrganizationContract.getOrganizationId();
        final Long vcsOrganizationId = postStartDataProcessingJobForVcsOrganizationContract.getVcsOrganizationId();
        SymeoRunnable jobToStart =
                () -> dataProcessingJobAdapter.startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(
                        organizationId,
                        vcsOrganizationId
                );
        final String errorMessage = String.format("Error while starting data processing jobs for organizationId %s " +
                "and vcsOrganizationId %s", organizationId, vcsOrganizationId);
        return runJobAndReturnResponseEntity(X_SYMEO_JOB_KEY_X, jobToStart, errorMessage);
    }

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startAllDataCollectionJobs(final String X_SYMEO_JOB_KEY_X) {
        SymeoRunnable jobToStart = organizationJobFacade::startAll;
        final String errorMessage = "Error while starting all data collection jobs";
        return runJobAndReturnResponseEntity(X_SYMEO_JOB_KEY_X, jobToStart, errorMessage);
    }

    private ResponseEntity<DataProcessingSymeoErrorsContract> runJobAndReturnResponseEntity(final String X_SYMEO_JOB_KEY_X,
                                                                                            final SymeoRunnable jobToStart,
                                                                                            final String errorMessage) {
        try {
            if (!X_SYMEO_JOB_KEY_X.equals(jobApiKey)) {
                LOGGER.error("Unauthorized header key {} = {}", jobApiHeaderKey, X_SYMEO_JOB_KEY_X);
                return ResponseEntity.status(403).build();
            }
            jobToStart.run();
            return ResponseEntity.ok().build();
        } catch (SymeoException e) {
            LOGGER.error(errorMessage, e);
            return SymeoErrorContractMapper.mapSymeoExceptionToContract(() -> SymeoErrorContractMapper.dataProcessingExceptionToContracts(e), e);
        }
    }
}
