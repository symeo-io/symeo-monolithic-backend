package io.symeo.monolithic.backend.domain.bff.query;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.Metrics;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.PullRequestPieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@AllArgsConstructor
public class CurveQuery {
    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;
    private final OrganizationStorageAdapter organizationStorageAdapter;

    public PullRequestPieceCurveWithAverage computeTimeToMergeCurve(final Organization organization,
                                                                    final UUID teamId, final Date startDate,
                                                                    final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final int range = 1;
        final List<Date> rangeDates = DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final Optional<OrganizationSettings> optionalOrganizationSettings = organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isPresent()) {
            final List<String> excludedBranchRegexes = optionalOrganizationSettings.get()
                    .getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
            final List<PullRequestView> pullRequestLimitViews =
                    bffExpositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization,
                                    teamId, startDate, endDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates))
                            .map(PullRequestView::addTimeLimit)
                            .toList();
            return PullRequestPieceCurveWithAverage.buildPullRequestCurve(pullRequestLimitViews,
                    Integer.parseInt(currentTeamGoal.getValue()));
        } else {
            return null;
        }
    }

    public PullRequestPieceCurveWithAverage computePullRequestSizeCurve(final Organization organization,
                                                                        final UUID teamId,
                                                                        final Date startDate, final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final int range = 1;
        final List<Date> rangeDates = DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final Optional<OrganizationSettings> optionalOrganizationSettings = organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isPresent()) {
            final List<String> excludedBranchRegexes = optionalOrganizationSettings.get()
                    .getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
            final List<PullRequestView> pullRequestSizeViews =
                    bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                                    teamId, startDate, endDate)
                            .stream()
                            .filter(pullRequestView -> excludePullRequest(pullRequestView, excludedBranchRegexes))
                            .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates))
                            .map(PullRequestView::addSizeLimit)
                            .toList();
            return PullRequestPieceCurveWithAverage.buildPullRequestCurve(pullRequestSizeViews,
                    Integer.parseInt(currentTeamGoal.getValue()));
        } else {
            return null;
        }
    }

    public Metrics computePullRequestSizeMetrics(Organization organization, UUID teamId, Date startDate,
                                                 Date endDate) throws SymeoException {
        final Optional<TeamGoal> optionalCurrentTeamGoal =
                teamGoalFacadeAdapter.getOptionalTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());

        if (optionalOrganizationSettings.isPresent()) {
            final List<String> excludedBranchRegexes = optionalOrganizationSettings.get()
                    .getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
            final List<PullRequestView> currentPullRequestViews =
                    bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                            teamId, startDate, endDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .toList();
            final Date previousStartDate = DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate,
                    endDate,
                    organization.getTimeZone());
            final List<PullRequestView> previousPullRequestViews =
                    bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                            teamId, previousStartDate,
                            startDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .toList();

            return Metrics.buildSizeMetricsFromPullRequests(optionalCurrentTeamGoal.map(TeamGoal::getValueAsInteger)
                            .orElse(1000), endDate, startDate,
                    previousStartDate, currentPullRequestViews,
                    previousPullRequestViews);
        } else {
            return null;
        }
    }

    public Metrics computePullRequestTimeToMergeMetrics(Organization organization, UUID teamId, Date startDate,
                                                        Date endDate) throws SymeoException {
        final Optional<TeamGoal> optionalCurrentTeamGoal =
                teamGoalFacadeAdapter.getOptionalTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());

        if (optionalOrganizationSettings.isPresent()) {
            final List<String> excludedBranchRegexes = optionalOrganizationSettings.get()
                    .getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
            final List<PullRequestView> currentPullRequestViews =
                    bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                            teamId, startDate, endDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .toList();
            final Date previousStartDate = DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate,
                    endDate,
                    organization.getTimeZone());
            final List<PullRequestView> previousPullRequestViews =
                    bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                            teamId, previousStartDate,
                            startDate)
                            .stream()
                            .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                            .toList();
            
            return Metrics.buildTimeToMergeMetricsFromPullRequests(optionalCurrentTeamGoal.map(TeamGoal::getValueAsInteger)
                            .orElse(5), endDate,
                    startDate, previousStartDate, currentPullRequestViews,
                    previousPullRequestViews);
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
