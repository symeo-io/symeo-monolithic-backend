package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.LeadTimeContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.LeadTimeMetricsFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.LeadTimeApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContract;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.LeadTimeContractMapper.errorToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "LeadTime"))
@AllArgsConstructor
public class LeadTimeRestApiAdapter implements LeadTimeApi {

    private final AuthenticationService authenticationService;
    private final LeadTimeMetricsFacadeAdapter leadTimeMetricsFacadeAdapter;

    @Override
    public ResponseEntity<LeadTimeResponseContract> getLeadTimeMetrics(UUID teamId, String startDate, String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(LeadTimeContractMapper.toContract(leadTimeMetricsFacadeAdapter
                    .computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(authenticatedUser.getOrganization(),
                            teamId, stringToDate(startDate),
                            stringToDate(endDate))));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> errorToContract(e), e);
        }
    }
}
