package catlean.monolithic.backend.rest.api.adapter;

import fr.catlean.monolithic.backend.contract.api.DataProcessingJobApi;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class DataProcessingRestApiAdapter implements DataProcessingJobApi {

    private final DataProcessingJobAdapter dataProcessingJobAdapter;

    @Override
    public ResponseEntity<Void> startDataProcessingJob() {
        try {
            dataProcessingJobAdapter.start("dalma");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
