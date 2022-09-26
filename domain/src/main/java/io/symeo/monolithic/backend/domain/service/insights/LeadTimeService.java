package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.port.in.LeadTimeFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics.buildFromCurrentAndPreviousLeadTimes;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class LeadTimeService implements LeadTimeFacadeAdapter {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final OrganizationSettingsFacade organizationSettingsFacade;

    @Override
    public Optional<LeadTimeMetrics> computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                           final UUID teamId,
                                                                                           final Date startDate,
                                                                                           final Date endDate) throws SymeoException {
        final OrganizationSettings organizationSettings =
                organizationSettingsFacade.getOrganizationSettingsForOrganization(organization);
        final List<String> excludeBranchRegexes =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        final List<PullRequestView> currentPullRequestViews =
                expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                                endDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .collect(Collectors.toList());
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone());
        final List<PullRequestView> previousPullRequestViews =
                expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .toList();
        final List<Commit> allCommitsFromPreviousStartDate =
                expositionStorageAdapter.readAllCommitsForTeamIdFromStartDate(teamId, previousStartDate);
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        if (nonNull(pullRequestMergedOnBranchRegex)) {
            final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);

            final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                    expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                    startDate, endDate)
                            .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

            final Optional<AverageLeadTime> currentLeadTime =
                    AverageLeadTime.buildForPullRequestMergedOnBranchRegexSettings(
                            currentPullRequestViews,
                            pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                            allCommitsFromPreviousStartDate
                    );


            final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                    expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                    previousStartDate, startDate)
                            .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

            final Optional<AverageLeadTime> previousLeadTime =
                    AverageLeadTime.buildForPullRequestMergedOnBranchRegexSettings(
                            previousPullRequestViews,
                            previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                            allCommitsFromPreviousStartDate
                    );

            return buildFromCurrentAndPreviousLeadTimes(currentLeadTime, previousLeadTime, previousStartDate, startDate,
                    endDate);
        }
        return Optional.empty();
    }


    private Boolean excludePullRequest(final PullRequestView pullRequestView, final List<String> excludeBranchRegexes) {
        return excludeBranchRegexes.stream().anyMatch(
                regex -> !Pattern.compile(regex).matcher(pullRequestView.getHead()).find()
        );
    }
}
