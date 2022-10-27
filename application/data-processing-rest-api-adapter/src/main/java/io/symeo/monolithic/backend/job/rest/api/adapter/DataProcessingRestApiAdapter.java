package io.symeo.monolithic.backend.job.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.data.processing.contract.api.DataProcessingJobApi;
import io.symeo.monolithic.backend.data.processing.contract.api.model.DataProcessingSymeoErrorsContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.PostStartDataProcessingJobContract;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.job.rest.api.adapter.mapper.SymeoErrorContractMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "DataProcessingJob"))
@Slf4j
public class DataProcessingRestApiAdapter implements DataProcessingJobApi {

    private final DataProcessingJobAdapter dataProcessingJobAdapter;
    private final String jobApiKey;
    private final String jobApiHeaderKey;

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startDataProcessingJobForRepositoryIds(String X_SYMEO_JOB_KEY_X, PostStartDataProcessingJobContract postStartDataProcessingJobContract) {
        return DataProcessingJobApi.super.startDataProcessingJobForRepositoryIds(X_SYMEO_JOB_KEY_X,
                postStartDataProcessingJobContract);
    }

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> startAllDataCollectionJobs(String X_SYMEO_JOB_KEY_X) {
        try {
            if (!X_SYMEO_JOB_KEY_X.equals(jobApiKey)) {
                LOGGER.error("Unauthorized header key {} = {}", jobApiHeaderKey, X_SYMEO_JOB_KEY_X);
                return ResponseEntity.status(403).build();
            }
            dataProcessingJobAdapter.startAll();
            return ResponseEntity.ok().build();
        } catch (SymeoException e) {
            LOGGER.error("Error while starting all data collection jobs", e);
            return SymeoErrorContractMapper.mapSymeoExceptionToContract(() -> SymeoErrorContractMapper.dataProcessingExceptionToContracts(e), e);
        }
    }
}
