package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.bff.contract.api.TestingApi;
import io.symeo.monolithic.backend.bff.contract.api.model.TestingResponseContract;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.TestingMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.TestingContractMapper.toContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.TestingContractMapper.errorToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Testing"))
@AllArgsConstructor
public class TestingRestApiAdapter implements TestingApi {

    private final AuthenticationService authenticationService;

    private final TestingMetricsFacadeAdapter testingMetricsFacadeAdapter;

    @Override
    public ResponseEntity<TestingResponseContract> getTestingMetrics(UUID teamId, String startDate,
                                                              String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toContract(testingMetricsFacadeAdapter
                    .computeTestingMetricsForTeamIdFromStartDateToEndDate(authenticatedUser.getOrganization(),
                            teamId, stringToDate(startDate),
                            stringToDate(endDate))));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> errorToContract(symeoException), symeoException);
        }
    }
}
