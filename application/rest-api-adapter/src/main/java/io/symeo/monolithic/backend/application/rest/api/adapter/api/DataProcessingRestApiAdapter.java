package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.data.processing.contract.api.DataProcessingJobApi;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "DataProcessingJob"))
@Slf4j
public class DataProcessingRestApiAdapter implements DataProcessingJobApi {

    private final DataProcessingJobAdapter dataProcessingJobAdapter;
    private final String jobApiKey;

    @Override
    public ResponseEntity<Void> startDataProcessingJob(final UUID organizationId) {
        try {
            dataProcessingJobAdapter.start(organizationId);
            return ResponseEntity.ok().build();
        } catch (SymeoException e) {
            LOGGER.error("Error while starting data collection job for organizationId {}", organizationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Void> startAllDataCollectionJobs(String X_SYMEO_JOB_KEY_X) {
        try {
            if (!X_SYMEO_JOB_KEY_X.equals(jobApiKey)) {
                LOGGER.error("Unauthorized X_SYMEO_JOB_KEY_X {}", X_SYMEO_JOB_KEY_X);
                return ResponseEntity.status(403).build();
            }
            dataProcessingJobAdapter.startAll();
            return ResponseEntity.ok().build();
        } catch (SymeoException e) {
            LOGGER.error("Error while starting all data collection jobs", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
