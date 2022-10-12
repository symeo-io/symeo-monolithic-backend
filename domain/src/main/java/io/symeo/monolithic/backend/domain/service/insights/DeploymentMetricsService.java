package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.Deployment;
import io.symeo.monolithic.backend.domain.model.insight.DeploymentMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.domain.port.in.DeploymentMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.model.insight.DeploymentMetrics.*;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class DeploymentMetricsService implements DeploymentMetricsFacadeAdapter {

    private final OrganizationSettingsFacade organizationSettingsFacade;

    private final ExpositionStorageAdapter expositionStorageAdapter;

    private final DeploymentService deploymentService;

    @Override
    public Optional<DeploymentMetrics> computeDeploymentMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                               UUID teamId,
                                                                                               Date startDate,
                                                                                               Date endDate) throws SymeoException {
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());
        final OrganizationSettings organizationSettings =
                organizationSettingsFacade.getOrganizationSettingsForOrganization(organization);
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();

        if (nonNull(pullRequestMergedOnBranchRegex)) {
            return getDeploymentMetricsForPullRequestMergedOnBranchRegex(teamId, startDate, endDate, previousStartDate,
                    pullRequestMergedOnBranchRegex);
        } else if (nonNull(tagRegex)) {
            return getDeploymentMetricsForDeployOnTagRegex(teamId, startDate, endDate, previousStartDate,
                    tagRegex);
        }
        LOGGER.warn("DeploymentMetrics not computed due to missing delivery settings for organization {} and teamId {} " +
                        "and organizationSettings {}",
                organization, teamId, organizationSettings);
        return Optional.empty();
    }

    private Optional<DeploymentMetrics> getDeploymentMetricsForPullRequestMergedOnBranchRegex(UUID teamId,
                                                                                              Date startDate,
                                                                                              Date endDate,
                                                                                              Date previousStartDate,
                                                                                              String pullRequestMergedOnBranchRegex) throws SymeoException {

        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);
        final Long numberOfDaysBetweenStartDateAndEndDate = getNumberOfDaysBetweenStartDateAndEndDate(startDate, endDate);

        final List<PullRequestView> currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate)
                        .stream()
                        .filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();
        final Optional<Deployment> optionalCurrentDeployment =
                deploymentService.buildForPullRequestMergedOnBranchRegexSettings(currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId, previousStartDate, startDate)
                        .stream()
                        .filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();
        final Optional<Deployment> optionalPreviousDeployment =
                deploymentService.buildForPullRequestMergedOnBranchRegexSettings(previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        return buildFromCurrentAndPreviousDeployment(optionalCurrentDeployment, optionalPreviousDeployment, previousStartDate, startDate,
                endDate);
    }

    private Optional<DeploymentMetrics> getDeploymentMetricsForDeployOnTagRegex(UUID teamId,
                                                                                Date startDate,
                                                                                Date endDate,
                                                                                Date previousStartDate,
                                                                                String deployOnTagRegex) throws SymeoException {
        final Pattern tagPattern = Pattern.compile(deployOnTagRegex);
        final Long numberOfDaysBetweenStartDateAndEndDate = getNumberOfDaysBetweenStartDateAndEndDate(startDate, endDate);

        final List<String> commitsShaForTagsMatchingTeamIdAndDeployTagRegex =
                expositionStorageAdapter.findTagsForTeamId(teamId)
                        .stream()
                        .filter(tag -> tagPattern.matcher(tag.getName()).find())
                        .map(Tag::getCommitSha)
                        .toList();
        final List<Commit> currentCommitsMatchingTagRegexBetweenStartDateAndEndDate =
                expositionStorageAdapter.readCommitsMatchingShaListBetweenStartDateAndEndDate(commitsShaForTagsMatchingTeamIdAndDeployTagRegex,
                        startDate, endDate);
        final List<Commit> previousCommitMatchingTagRegexBetweenStartDateAndEndDate =
                expositionStorageAdapter.readCommitsMatchingShaListBetweenStartDateAndEndDate(commitsShaForTagsMatchingTeamIdAndDeployTagRegex,
                        previousStartDate, startDate);

        final Optional<Deployment> optionalCurrentDeployment =
                deploymentService.buildForTagRegexSettings(
                        currentCommitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate
                );
        final Optional<Deployment> optionalPreviousDeployment =
                deploymentService.buildForTagRegexSettings(
                        previousCommitMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate
                );
        return buildFromCurrentAndPreviousDeployment(optionalCurrentDeployment, optionalPreviousDeployment, previousStartDate, startDate,
                endDate);
    }
}
