package catlean.monolithic.backend.rest.api.adapter.api;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper;
import catlean.monolithic.backend.rest.api.adapter.mapper.CurveMapper;
import catlean.monolithic.backend.rest.api.adapter.mapper.TeamGoalContractMapper;
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

import static catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper.catleanExceptionToContracts;
import static catlean.monolithic.backend.rest.api.adapter.mapper.CurveMapper.curveToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper.domainToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper.errorToContract;
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
            return internalServerError().body(catleanExceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getTimeToMergeCurve(final UUID teamId, final String startDate,
                                                                        final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(curveToContract(curveQuery.computeTimeToMergeCurve(authenticatedUser.getOrganization(),
                    teamId)));
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
            return domainToContract(histogramQuery.computePullRequestTimeToMergeHistogram(authenticatedUser.getOrganization(),
                    teamId));
        } catch (CatleanException e) {
            return errorToContract(e);
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getPullRequestSizeCurve(final UUID teamId, final String startDate,
                                                                            final String endDate) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(curveToContract(curveQuery.computePullRequestSizeCurve(authenticatedUser.getOrganization(),
                    teamId)));
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
            return domainToContract(histogramQuery.computePullRequestSizeHistogram(authenticatedUser.getOrganization(),
                    teamId));
        } catch (CatleanException e) {
            return errorToContract(e);
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
            return internalServerError().body(catleanExceptionToContracts(e));
        }
    }
}
