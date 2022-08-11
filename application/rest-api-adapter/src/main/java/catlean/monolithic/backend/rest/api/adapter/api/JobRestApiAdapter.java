package catlean.monolithic.backend.rest.api.adapter.api;

import fr.catlean.monolithic.backend.domain.port.in.JobFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.JobApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.LastTwoJobsResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Job"))
@AllArgsConstructor
public class JobRestApiAdapter implements JobApi {

    public final JobFacadeAdapter jobFacadeAdapter;

    @Override
    public ResponseEntity<LastTwoJobsResponseContract> getLastTwoJobsForCode(String jobCode) {
        return JobApi.super.getLastTwoJobsForCode(jobCode);
    }
}
