package fr.catlean.monolithic.backend.application.rest.api.adapter.api;

import fr.catlean.monolithic.backend.data.processing.contract.api.DataProcessingJobApi;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
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

    @Override
    public ResponseEntity<Void> startDataProcessingJob(String organizationName) {
        try {
            dataProcessingJobAdapter.start(organizationName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LOGGER.error("{}", e);
            return ResponseEntity.internalServerError().build();
        }

    }

}
