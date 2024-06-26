package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeCurveFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.bff.contract.api.CycleTimeApi;
import io.symeo.monolithic.backend.bff.contract.api.model.CycleTimeCurveResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.CycleTimePiecesResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.CycleTimeResponseContract;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.CycleTimeContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.CycleTimeContractMapper.errorToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "CycleTime"))
@AllArgsConstructor
public class CycleTimeRestApiAdapter implements CycleTimeApi {

    private final AuthenticationService authenticationService;
    private final CycleTimeMetricsFacadeAdapter cycleTimeMetricsFacadeAdapter;
    private final CycleTimeCurveFacadeAdapter cycleTimeCurveFacadeAdapter;

    @Override
    public ResponseEntity<CycleTimeResponseContract> getCycleTimeMetrics(UUID teamId, String startDate,
                                                                         String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toContract(cycleTimeMetricsFacadeAdapter
                    .computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(authenticatedUser.getOrganization(),
                            teamId, stringToDate(startDate),
                            stringToDate(endDate))));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> errorToContract(e), e);
        }
    }

    @Override
    public ResponseEntity<CycleTimePiecesResponseContract> getCycleTimePieces(UUID teamId, Integer pageIndex,
                                                                              Integer pageSize, String startDate,
                                                                              String endDate, String sortBy,
                                                                              String sortDir) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toPiecesContract(cycleTimeMetricsFacadeAdapter
                    .computeCycleTimePiecesForTeamIdFromStartDateToEndDate(authenticatedUser.getOrganization(),
                            teamId, stringToDate(startDate), stringToDate(endDate), pageIndex, pageSize, sortBy,
                            sortDir)));
        } catch (SymeoException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ResponseEntity<CycleTimeCurveResponseContract> getCycleTimeCurve(UUID teamId, String startDate,
                                                                            String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toCurveContract(cycleTimeCurveFacadeAdapter.computeCycleTimePieceCurveWithAverage(
                    authenticatedUser.getOrganization(), teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (SymeoException e) {
            throw new RuntimeException(e);
        }
    }
}
