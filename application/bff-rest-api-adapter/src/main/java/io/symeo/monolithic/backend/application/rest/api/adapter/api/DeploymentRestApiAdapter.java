package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.bff.contract.api.DeploymentApi;
import io.symeo.monolithic.backend.bff.contract.api.model.DeploymentResponseContract;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.DeploymentMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.DeploymentContractMapper.errorToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.DeploymentContractMapper.toContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Deployment"))
@AllArgsConstructor
public class DeploymentRestApiAdapter implements DeploymentApi {

    private final AuthenticationService authenticationService;

    private final DeploymentMetricsFacadeAdapter deploymentMetricsFacadeAdapter;

    @Override
    public ResponseEntity<DeploymentResponseContract> getDeploymentMetrics(UUID teamId, String startDate,
                                                                           String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toContract(deploymentMetricsFacadeAdapter
                    .computeDeploymentMetricsForTeamIdFromStartDateToEndDate(authenticatedUser.getOrganization(),
                            teamId, stringToDate(startDate),
                            stringToDate(endDate))));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> errorToContract(symeoException), symeoException);
        }
    }
}
