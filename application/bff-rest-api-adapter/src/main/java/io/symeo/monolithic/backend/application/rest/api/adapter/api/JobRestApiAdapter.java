package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.bff.contract.api.JobApi;
import io.symeo.monolithic.backend.domain.bff.port.in.JobFacadeAdapter;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Job"))
@AllArgsConstructor
public class JobRestApiAdapter implements JobApi {

    public final AuthenticationService authenticationService;
    public final JobFacadeAdapter jobFacadeAdapter;

}
