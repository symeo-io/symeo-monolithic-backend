package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.JobContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForOrganizationAndTeamJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.JobFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.JobApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.LastJobsResponseContract;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Job"))
@AllArgsConstructor
public class JobRestApiAdapter implements JobApi {

    public final AuthenticationService authenticationService;
    public final JobFacadeAdapter jobFacadeAdapter;

    @Override
    public ResponseEntity<LastJobsResponseContract> getLastTwoVcsDataCollectionJobsForTeamId(UUID teamId) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(JobContractMapper.domainToContract(jobFacadeAdapter.findLastJobsForCodeAndOrganizationAndLimitAndTeamId(
                    CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE,
                    authenticatedUser.getOrganization().getId(),
                    teamId,
                    2)));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> JobContractMapper.errorToContract(e), e);
        }
    }
}
