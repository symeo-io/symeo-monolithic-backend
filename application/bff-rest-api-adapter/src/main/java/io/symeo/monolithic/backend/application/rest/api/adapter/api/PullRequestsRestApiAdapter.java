package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.port.in.PullRequestFacade;
import io.symeo.monolithic.backend.bff.contract.api.PullRequestsApi;
import io.symeo.monolithic.backend.bff.contract.api.model.PullRequestsResponseContract;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.PullRequestContractMapper.errorToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.PullRequestContractMapper.toContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "PullRequests"))
@AllArgsConstructor
public class PullRequestsRestApiAdapter implements PullRequestsApi {

    private final AuthenticationService authenticationService;
    private final PullRequestFacade pullRequestFacade;

    @Override
    public ResponseEntity<PullRequestsResponseContract> getPullRequestsForTeam(final UUID teamId,
                                                                               final String startDateString,
                                                                               final String endDateString,
                                                                               final Integer pageIndex,
                                                                               final Integer pageSize,
                                                                               final String sortBy,
                                                                               final String sortDir) {
        try {
            final Date endDate = stringToDate(endDateString);
            return ok(toContract(pullRequestFacade.getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                            stringToDate(startDateString),
                            endDate, pageIndex, pageSize, sortBy, sortDir),
                    authenticationService.getAuthenticatedUser().getOrganization().getTimeZone().toZoneId(), endDate));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> errorToContract(e), e);
        }
    }
}
