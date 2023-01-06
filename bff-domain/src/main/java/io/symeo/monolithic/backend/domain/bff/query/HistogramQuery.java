package io.symeo.monolithic.backend.domain.bff.query;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.PullRequestHistogram;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.insights.PullRequestHistogramService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@AllArgsConstructor
public class HistogramQuery {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;
    private final PullRequestHistogramService pullRequestHistogramService;
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private static final int RANGE = 7;

    public PullRequestHistogram computePullRequestTimeToMergeHistogram(final Organization organization,
                                                                       final UUID teamId, final Date startDate,
                                                                       final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final Optional<OrganizationSettings> optionalOrganizationSettings = organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isPresent()) {
            final List<String> excludedBranchRegexes = optionalOrganizationSettings.get()
                    .getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
            final List<PullRequestView> pullRequests =
                    bffExpositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization, teamId, startDate, endDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .toList();
            final List<Date> rangeDates =
                    DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate, RANGE,
                            organization.getTimeZone());

            return pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.TIME_LIMIT, pullRequests,
                    organization,
                    currentTeamGoal,
                    rangeDates,
                    RANGE);
        } else {
            return null;
        }
    }

    public PullRequestHistogram computePullRequestSizeHistogram(final Organization organization, final UUID teamId,
                                                                final Date startDate, final Date endDate)
            throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final Optional<OrganizationSettings> optionalOrganizationSettings = organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isPresent()) {
            final List<String> excludedBranchRegexes = optionalOrganizationSettings.get()
                    .getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
            final List<PullRequestView> pullRequests =
                    bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId,
                            startDate, endDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .toList();
            final List<Date> rangeDates =
                    DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate, RANGE,
                            organization.getTimeZone());

            return pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.SIZE_LIMIT, pullRequests,
                    organization,
                    currentTeamGoal,
                    rangeDates,
                    RANGE);
        } else {
            return null;
        }
    }

    private static boolean excludePullRequest(PullRequestView pullRequest, List<String> excludedBranchRegexes) {
        return excludedBranchRegexes.isEmpty() || excludedBranchRegexes.stream().noneMatch(
                regex -> Pattern.compile(regex).matcher(pullRequest.getHead()).find()
        );
    }
}
