package io.symeo.monolithic.backend.job.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.data.processing.contract.api.DataProcessingJobApi;
import io.symeo.monolithic.backend.data.processing.contract.api.model.DataProcessingSymeoErrorsContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForOrganizationContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForTeamContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobForVcsOrganizationContract;
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
        SymeoRunnable jobToStart =
                () -> dataProcessingJobAdapter.startToCollectVcsDataForOrganizationIdAndRepositoryIds(
                        organizationId,
                        repositoryIds
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
        SymeoRunnable jobToStart =
                () -> dataProcessingJobAdapter.startToCollectVcsDataForOrganizationIdAndTeamIdAndRepositoryIds(
                        organizationId,
                        teamId,
                        repositoryIds
                );
        final String errorMessage = String.format("Error while starting data processing jobs for organizationId %s " +
                "and teamId %s and repositoryIds %s", organizationId, teamId, repositoryIds);
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
