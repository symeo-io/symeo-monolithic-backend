package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.PullRequestCurveContractMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.PullRequestHistogramContractMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.TeamGoalContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.query.CurveQuery;
import io.symeo.monolithic.backend.domain.query.HistogramQuery;
import io.symeo.monolithic.backend.frontend.contract.api.GoalsApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.MetricsContractMapper.errorsToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.MetricsContractMapper.metricsToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@Tags(@Tag(name = "Goals"))
@AllArgsConstructor
@RestController
public class TeamGoalRestApiAdapter implements GoalsApi {

    private final HistogramQuery histogramQuery;
    private final AuthenticationService authenticationService;
    private final CurveQuery curveQuery;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;

    @Override
    public ResponseEntity<SymeoErrorsContract> createTeamGoal(final PostCreateTeamGoalsRequest postCreateTeamGoalsRequest) {
        try {
            teamGoalFacadeAdapter.createTeamGoalForTeam(postCreateTeamGoalsRequest.getTeamId(),
                    postCreateTeamGoalsRequest.getStandardCode(), postCreateTeamGoalsRequest.getValue());
            return ok().build();
        } catch (SymeoException e) {
            return internalServerError().body(SymeoErrorContractMapper.exceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getTimeToMergeCurve(final UUID teamId, final String startDate,
                                                                        final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(PullRequestCurveContractMapper.curveToContract(curveQuery.computeTimeToMergeCurve(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (SymeoException e) {
            return internalServerError().body(PullRequestCurveContractMapper.errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<GetHistogramResponseContract> getTimeToMergeHistogram(final UUID teamId,
                                                                                final String startDate,
                                                                                final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return PullRequestHistogramContractMapper.domainToContract(histogramQuery.computePullRequestTimeToMergeHistogram(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate)));
        } catch (SymeoException e) {
            return PullRequestHistogramContractMapper.errorToContract(e);
        }
    }

    @Override
    public ResponseEntity<MetricsResponseContract> getTimeToMergeMetrics(UUID teamId, String startDate,
                                                                         String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(metricsToContract(curveQuery.computePullRequestTimeToMergeMetrics(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (SymeoException e) {
            return internalServerError().body(errorsToContract(e));
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getPullRequestSizeCurve(final UUID teamId, final String startDate,
                                                                            final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(PullRequestCurveContractMapper.curveToContract(curveQuery.computePullRequestSizeCurve(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (SymeoException e) {
            return internalServerError().body(PullRequestCurveContractMapper.errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<MetricsResponseContract> getPullRequestSizeMetrics(UUID teamId, String startDate,
                                                                             String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(metricsToContract(curveQuery.computePullRequestSizeMetrics(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (SymeoException e) {
            return internalServerError().body(errorsToContract(e));
        }
    }

    @Override
    public ResponseEntity<GetHistogramResponseContract> getPullRequestSizeHistogram(final UUID teamId,
                                                                                    final String startDate,
                                                                                    final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return PullRequestHistogramContractMapper.domainToContract(histogramQuery.computePullRequestSizeHistogram(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate)));
        } catch (SymeoException e) {
            return PullRequestHistogramContractMapper.errorToContract(e);
        }
    }

    @Override
    public ResponseEntity<TeamGoalsResponseContract> getTeamGoals(UUID teamId) {
        try {
            return ok(TeamGoalContractMapper.domainToContract(teamGoalFacadeAdapter.readForTeamId(teamId)));
        } catch (SymeoException e) {
            return internalServerError().body(TeamGoalContractMapper.errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> deleteTeamGoal(UUID teamGoalId) {
        try {
            teamGoalFacadeAdapter.deleteTeamGoalForId(teamGoalId);
            return ok().build();
        } catch (SymeoException e) {
            return internalServerError().body(SymeoErrorContractMapper.exceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> updateTeamGoal(PatchTeamGoalsRequest patchTeamGoalsRequest) {
        try {
            teamGoalFacadeAdapter.updateTeamGoalForTeam(patchTeamGoalsRequest.getId(),
                    patchTeamGoalsRequest.getValue());
            return ok().build();
        } catch (SymeoException e) {
            return internalServerError().body(SymeoErrorContractMapper.exceptionToContracts(e));
        }
    }

}
