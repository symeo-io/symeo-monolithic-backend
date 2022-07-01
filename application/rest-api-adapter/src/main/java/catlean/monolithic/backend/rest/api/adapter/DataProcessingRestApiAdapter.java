package catlean.monolithic.backend.rest.api.adapter;

import fr.catlean.monolithic.backend.data.processing.contract.api.DataProcessingJobApi;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "DataProcessingJob"))
public class DataProcessingRestApiAdapter implements DataProcessingJobApi {

    private final DataProcessingJobAdapter dataProcessingJobAdapter;

    @Override
    public ResponseEntity<Void> startDataProcessingJob() {
        try {
            dataProcessingJobAdapter.start("armis");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
