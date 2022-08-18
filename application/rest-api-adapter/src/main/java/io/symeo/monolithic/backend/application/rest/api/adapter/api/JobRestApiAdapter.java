package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.JobFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.JobApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.LastJobsResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.JobContractMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Job"))
@AllArgsConstructor
public class JobRestApiAdapter implements JobApi {

    public final AuthenticationService authenticationService;
    public final JobFacadeAdapter jobFacadeAdapter;

    @Override
    public ResponseEntity<LastJobsResponseContract> getLastTwoJobsForCode(String jobCode) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(JobContractMapper.domainToContract(jobFacadeAdapter.findLastJobsForCodeAndOrganizationAndLimit(jobCode,
                    authenticatedUser.getOrganization(),
                    2), authenticatedUser.getOrganization()));
        } catch (SymeoException e) {
            return internalServerError().body(JobContractMapper.errorToContract(e));
        }

    }
}
