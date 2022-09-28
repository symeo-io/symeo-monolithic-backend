package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.AverageCycleTime;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.domain.port.in.CycleTimeMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.model.insight.CycleTimeMetrics.buildFromCurrentAndPreviousCycleTimes;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class CycleTimeMetricsMetricsService implements CycleTimeMetricsFacadeAdapter {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final OrganizationSettingsFacade organizationSettingsFacade;
    private final CycleTimeService cycleTimeService;

    @Override
    public Optional<CycleTimeMetrics> computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                             final UUID teamId,
                                                                                             final Date startDate,
                                                                                             final Date endDate) throws SymeoException {
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone());

        final OrganizationSettings organizationSettings =
                organizationSettingsFacade.getOrganizationSettingsForOrganization(organization);
        final List<String> excludeBranchRegexes =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();

        if (nonNull(pullRequestMergedOnBranchRegex)) {
            return getCycleTimeMetricsForPullRequestMergedOnBranchRegex(teamId, startDate, endDate, previousStartDate,
                    pullRequestMergedOnBranchRegex, excludeBranchRegexes);
        } else if (nonNull(tagRegex)) {
            return getCycleTimeMetricsForDeployOnTagRegex(teamId, startDate, endDate, previousStartDate, tagRegex,
                    excludeBranchRegexes);
        }
        LOGGER.warn("CycleTimeMetrics not computed due to missing delivery settings for organization {} and teamId {} " +
                        "and organizationSettings {}",
                organization, teamId, organizationSettings);
        return Optional.empty();
    }

    private Optional<CycleTimeMetrics> getCycleTimeMetricsForPullRequestMergedOnBranchRegex(final UUID teamId,
                                                                                           final Date startDate,
                                                                                           final Date endDate,
                                                                                           final Date previousStartDate,
                                                                                           final String pullRequestMergedOnBranchRegex,
                                                                                           final List<String> excludeBranchRegexes) throws SymeoException {
        final List<PullRequestView> currentPullRequestViews =
                expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                                endDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .collect(Collectors.toList());
        final List<PullRequestView> previousPullRequestViews =
                expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .toList();
        final List<Commit> allCommitsUntilEndDate =
                expositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                startDate, endDate)
                        .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

        final Optional<AverageCycleTime> currentCycleTime =
                cycleTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                        currentPullRequestViews,
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        allCommitsUntilEndDate
                );


        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                previousStartDate, startDate)
                        .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

        final Optional<AverageCycleTime> previousCycleTime =
                cycleTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                        previousPullRequestViews,
                        previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        allCommitsUntilEndDate
                );

        return buildFromCurrentAndPreviousCycleTimes(currentCycleTime, previousCycleTime, previousStartDate, startDate,
                endDate);
    }

    private Optional<CycleTimeMetrics> getCycleTimeMetricsForDeployOnTagRegex(final UUID teamId,
                                                                             final Date startDate,
                                                                             final Date endDate,
                                                                             final Date previousStartDate,
                                                                             final String deployOnTagRegex,
                                                                             final List<String> excludeBranchRegexes) throws SymeoException {
        final List<PullRequestView> currentPullRequestViews =
                expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                                endDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .collect(Collectors.toList());
        final List<PullRequestView> previousPullRequestViews =
                expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .toList();
        final List<Commit> allCommitsUntilEndDate =
                expositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern tagPattern = Pattern.compile(deployOnTagRegex);

        final List<Tag> tagsMatchingDeployTagRegex =
                expositionStorageAdapter.findTagsForTeamId(teamId)
                        .stream()
                        .filter(tag -> tagPattern.matcher(tag.getName()).find())
                        .toList();

        final Optional<AverageCycleTime> currentCycleTime =
                cycleTimeService.buildForTagRegexSettings(
                        currentPullRequestViews,
                        tagsMatchingDeployTagRegex,
                        allCommitsUntilEndDate
                );

        final Optional<AverageCycleTime> previousCycleTime =
                cycleTimeService.buildForTagRegexSettings(
                        previousPullRequestViews,
                        tagsMatchingDeployTagRegex,
                        allCommitsUntilEndDate
                );
        return buildFromCurrentAndPreviousCycleTimes(currentCycleTime, previousCycleTime, previousStartDate, startDate,
                endDate);
    }


    private Boolean excludePullRequest(final PullRequestView pullRequestView, final List<String> excludeBranchRegexes) {
        return excludeBranchRegexes.isEmpty() || excludeBranchRegexes.stream().anyMatch(
                regex -> !Pattern.compile(regex).matcher(pullRequestView.getHead()).find()
        );
    }
}
