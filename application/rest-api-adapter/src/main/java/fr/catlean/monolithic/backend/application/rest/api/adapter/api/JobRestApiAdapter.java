package fr.catlean.monolithic.backend.application.rest.api.adapter.api;

import fr.catlean.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.JobFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.JobApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.LastJobsResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.JobContractMapper.domainToContract;
import static fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.JobContractMapper.errorToContract;
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
            return ok(domainToContract(jobFacadeAdapter.findLastJobsForCodeAndOrganizationAndLimit(jobCode,
                    authenticatedUser.getOrganization(),
                    2), authenticatedUser.getOrganization()));
        } catch (CatleanException e) {
            return internalServerError().body(errorToContract(e));
        }

    }
}
