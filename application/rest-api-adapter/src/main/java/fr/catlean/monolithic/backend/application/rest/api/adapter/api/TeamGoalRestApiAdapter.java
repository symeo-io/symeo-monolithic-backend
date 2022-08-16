package fr.catlean.monolithic.backend.application.rest.api.adapter.api;

import fr.catlean.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.CatleanErrorContractMapper;
import fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.PullRequestHistogramContractMapper;
import fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.CurveMapper;
import fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.TeamGoalContractMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.query.CurveQuery;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.frontend.contract.api.GoalsApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.CurveMapper.curveToContract;
import static fr.catlean.monolithic.backend.domain.helper.DateHelper.stringToDate;
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
    public ResponseEntity<CatleanErrorsContract> createTeamGoal(final PostCreateTeamGoalsRequest postCreateTeamGoalsRequest) {
        try {
            teamGoalFacadeAdapter.createTeamGoalForTeam(postCreateTeamGoalsRequest.getTeamId(),
                    postCreateTeamGoalsRequest.getStandardCode(), postCreateTeamGoalsRequest.getValue());
            return ok().build();
        } catch (CatleanException e) {
            return internalServerError().body(CatleanErrorContractMapper.catleanExceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getTimeToMergeCurve(final UUID teamId, final String startDate,
                                                                        final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(curveToContract(curveQuery.computeTimeToMergeCurve(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (CatleanException e) {
            return internalServerError().body(CurveMapper.errorToContract(e));
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
        } catch (CatleanException e) {
            return PullRequestHistogramContractMapper.errorToContract(e);
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getPullRequestSizeCurve(final UUID teamId, final String startDate,
                                                                            final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(curveToContract(curveQuery.computePullRequestSizeCurve(authenticatedUser.getOrganization(),
                    teamId, stringToDate(startDate), stringToDate(endDate))));
        } catch (CatleanException e) {
            return internalServerError().body(CurveMapper.errorToContract(e));
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
        } catch (CatleanException e) {
            return PullRequestHistogramContractMapper.errorToContract(e);
        }
    }

    @Override
    public ResponseEntity<TeamGoalsResponseContract> getTeamGoals(UUID teamId) {
        try {
            return ok(TeamGoalContractMapper.domainToContract(teamGoalFacadeAdapter.readForTeamId(teamId)));
        } catch (CatleanException e) {
            return internalServerError().body(TeamGoalContractMapper.errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<CatleanErrorsContract> deleteTeamGoal(UUID teamGoalId) {
        try {
            teamGoalFacadeAdapter.deleteTeamGoalForId(teamGoalId);
            return ok().build();
        } catch (CatleanException e) {
            return internalServerError().body(CatleanErrorContractMapper.catleanExceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<CatleanErrorsContract> updateTeamGoal(PatchTeamGoalsRequest patchTeamGoalsRequest) {
        try {
            teamGoalFacadeAdapter.updateTeamGoalForTeam(patchTeamGoalsRequest.getId(),
                    patchTeamGoalsRequest.getValue());
            return ok().build();
        } catch (CatleanException e) {
            return internalServerError().body(CatleanErrorContractMapper.catleanExceptionToContracts(e));
        }
    }
}
