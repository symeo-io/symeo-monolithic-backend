package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeFactory;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeCurveFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class CycleTimeCurveService implements CycleTimeCurveFacadeAdapter {

    private final OrganizationSettingsFacade organizationSettingsFacade;
    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final CycleTimeFactory cycleTimeFactory;


    public CycleTimePieceCurveWithAverage computeCycleTimePieceCurveWithAverage(Organization organization,
                                                                                UUID teamId,
                                                                                Date startDate,
                                                                                Date endDate) throws SymeoException {
        final int range = 1;
        final List<Date> rangeDates = getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final OrganizationSettings organizationSettings =
                organizationSettingsFacade.getOrganizationSettingsForOrganization(organization);
        final List<String> excludeBranchRegexes =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();
        if (nonNull(pullRequestMergedOnBranchRegex)) {
            return getCycleTimePieceCurveWithAverageForPullRequestMergedOnBranchRegex(teamId, startDate, endDate, rangeDates,
                    pullRequestMergedOnBranchRegex, excludeBranchRegexes);
        } else if (nonNull(tagRegex)) {
            return getCycleTimePieceCurveWithAverageForDeployOnTagRegex(teamId, startDate, endDate, rangeDates,
                    tagRegex, excludeBranchRegexes);
        }
        return CycleTimePieceCurveWithAverage.builder().build();
    }

    private CycleTimePieceCurveWithAverage getCycleTimePieceCurveWithAverageForPullRequestMergedOnBranchRegex(UUID teamId,
                                                                                                              Date startDate,
                                                                                                              Date endDate,
                                                                                                              List<Date> rangeDates,
                                                                                                              String pullRequestMergedOnBranchRegex,
                                                                                                              List<String> excludeBranchRegexes) throws SymeoException {

        final List<PullRequestView> currentPullRequestViews =
                getPullRequestViewsForTeamIdBetweenStartDateAndEndDateWithDateRanges(teamId, endDate, rangeDates, excludeBranchRegexes);
        final List<CommitView> allCommitsUntilEndDate =
                bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                startDate, endDate)
                        .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

        final List<CycleTime> cycleTimes = currentPullRequestViews
                .stream()
                .map(pullRequestView -> cycleTimeFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                        pullRequestView,
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        allCommitsUntilEndDate))
                .toList();
        return CycleTimePieceCurveWithAverage.buildPullRequestCurve(cycleTimes);
    }

    private CycleTimePieceCurveWithAverage getCycleTimePieceCurveWithAverageForDeployOnTagRegex(UUID teamId,
                                                                                                Date startDate,
                                                                                                Date endDate,
                                                                                                List<Date> rangeDates,
                                                                                                String tagRegex,
                                                                                                List<String> excludeBranchRegexes) throws SymeoException {
        final List<PullRequestView> currentPullRequestViews =
                getPullRequestViewsForTeamIdBetweenStartDateAndEndDateWithDateRanges(teamId, endDate, rangeDates, excludeBranchRegexes);
        final List<CommitView> allCommitsUntilEndDate =
                bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern tagPattern = Pattern.compile(tagRegex);

        final List<TagView> tagsMatchingDeployTagRegex =
                bffExpositionStorageAdapter.findTagsForTeamId(teamId)
                        .stream()
                        .filter(tag -> tagPattern.matcher(tag.getName()).find())
                        .toList();

        final List<CycleTime> cycleTimes = currentPullRequestViews
                .stream()
                .map(pullRequestView -> cycleTimeFactory.computeCycleTimeForTagRegexToDeploySettings(
                        pullRequestView,
                        tagsMatchingDeployTagRegex,
                        allCommitsUntilEndDate))
                .toList();
        return CycleTimePieceCurveWithAverage.buildPullRequestCurve(cycleTimes);
    }

    private List<PullRequestView> getPullRequestViewsForTeamIdBetweenStartDateAndEndDateWithDateRanges(UUID teamId, Date endDate, List<Date> rangeDates, List<String> excludeBranchRegexes) throws SymeoException {
        return bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                        endDate)
                .stream()
                .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                .map(pullRequestView -> pullRequestView.addStartDateRangeFromRangeDates(rangeDates))
                .toList();
    }

    private Boolean excludePullRequest(final PullRequestView pullRequestView, final List<String> excludeBranchRegexes) {
        return excludeBranchRegexes.isEmpty() || excludeBranchRegexes.stream().noneMatch(
                regex -> Pattern.compile(regex).matcher(pullRequestView.getHead()).find()
        );
    }

}
