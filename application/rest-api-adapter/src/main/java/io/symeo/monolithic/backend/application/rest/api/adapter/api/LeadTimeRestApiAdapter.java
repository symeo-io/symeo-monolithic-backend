package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.LeadTimeContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.LeadTimeFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.LeadTimeApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeCurveResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContract;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.LeadTimeCurveContractMapper.errorToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.LeadTimeCurveContractMapper.toContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "LeadTime"))
@AllArgsConstructor
public class LeadTimeRestApiAdapter implements LeadTimeApi {

    private final AuthenticationService authenticationService;
    private final LeadTimeFacadeAdapter leadTimeFacadeAdapter;

    @Override
    public ResponseEntity<LeadTimeResponseContract> getLeadTimeMetrics(UUID teamId, String startDate, String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(LeadTimeContractMapper.toContract(leadTimeFacadeAdapter
                    .computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(authenticatedUser.getOrganization(),
                            teamId, stringToDate(startDate),
                            stringToDate(endDate))));
        } catch (SymeoException e) {
            return internalServerError().body(LeadTimeContractMapper.errorToContract(e));
        }
    }

    @Deprecated
    @Override
    public ResponseEntity<LeadTimeCurveResponseContract> getLeadTimeCurve(UUID teamId, String startDate,
                                                                          String endDate) {
        try {
            return ok(
                    toContract(
                            leadTimeFacadeAdapter.computeLeadTimeCurvesForTeamIdFromStartDateAndEndDate(teamId,
                                    stringToDate(startDate),
                                    stringToDate(endDate))
                    ));
        } catch (SymeoException e) {
            return internalServerError().body(errorToContract(e));
        }
    }
}
