package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper;
import catlean.monolithic.backend.rest.api.adapter.mapper.TeamGoalContractMapper;
import catlean.monolithic.backend.rest.api.adapter.mapper.TimeToMergeCurveMapper;
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
import static catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper.domainToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper.errorToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.TimeToMergeCurveMapper.curveToContract;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.TIME_LIMIT;
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
    public ResponseEntity<CatleanErrorsContract> createTeamGoal(PostCreateTeamGoalsRequest postCreateTeamGoalsRequest) {
        try {
            teamGoalFacadeAdapter.createTeamGoalForTeam(postCreateTeamGoalsRequest.getTeamId(),
                    postCreateTeamGoalsRequest.getStandardCode(), postCreateTeamGoalsRequest.getValue());
            return ok().build();
        } catch (CatleanException e) {
            return internalServerError().body(catleanExceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<GetCurveResponseContract> getTimeToMergeCurve(UUID teamId) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(curveToContract(curveQuery.computeTimeToMergeCurve(authenticatedUser.getOrganization(),
                    teamId)));
        } catch (CatleanException e) {
            return internalServerError().body(TimeToMergeCurveMapper.errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<GetHistogramResponseContract> getTimeToMergeHistogram(UUID teamId) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return domainToContract(histogramQuery.computePullRequestHistogram(authenticatedUser.getOrganization(),
                    teamId, TIME_LIMIT));
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
