package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.Deployment;
import io.symeo.monolithic.backend.domain.bff.model.metric.DeploymentMetrics;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.DeploymentMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class DeploymentMetricsService implements DeploymentMetricsFacadeAdapter {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final OrganizationSettingsFacade organizationSettingsFacade;
    private final DeploymentService deploymentService;

    @Override
    public Optional<DeploymentMetrics> computeDeploymentMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                               UUID teamId,
                                                                                               Date startDate,
                                                                                               Date endDate) throws SymeoException {
        final Date previousStartDate = DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone());
        final OrganizationSettings organizationSettings =
                organizationSettingsFacade.getOrganizationSettingsForOrganization(organization);
        final DeployDetectionTypeDomainEnum deployDetectionType =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType();
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();

        if (!isNull(deployDetectionType) &&  nonNull(pullRequestMergedOnBranchRegex)) {
            return getDeploymentMetricsForPullRequestMergedOnBranchRegex(teamId, startDate, endDate, previousStartDate,
                    pullRequestMergedOnBranchRegex);
        } else if (!isNull(deployDetectionType) &&  nonNull(tagRegex)) {
            return getDeploymentMetricsForDeployOnTagRegex(teamId, startDate, endDate, previousStartDate,
                    tagRegex);
        }
        LOGGER.warn("DeploymentMetrics not computed due to missing delivery settings for organization {} and teamId " +
                        "{} " +
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
        final Long numberOfDaysBetweenStartDateAndEndDate =
                DateHelper.getNumberOfDaysBetweenStartDateAndEndDate(startDate, endDate);

        final List<PullRequestView> currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId, startDate,
                                endDate)
                        .stream()
                        .filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();
        final Optional<Deployment> optionalCurrentDeployment =
                deploymentService.buildForPullRequestMergedOnBranchRegexSettings(currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                previousStartDate, startDate)
                        .stream()
                        .filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();
        final Optional<Deployment> optionalPreviousDeployment =
                deploymentService.buildForPullRequestMergedOnBranchRegexSettings(previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        return DeploymentMetrics.buildFromCurrentAndPreviousDeployment(optionalCurrentDeployment,
                optionalPreviousDeployment, previousStartDate, startDate,
                endDate);
    }

    private Optional<DeploymentMetrics> getDeploymentMetricsForDeployOnTagRegex(UUID teamId,
                                                                                Date startDate,
                                                                                Date endDate,
                                                                                Date previousStartDate,
                                                                                String deployOnTagRegex) throws SymeoException {
        final Pattern tagPattern = Pattern.compile(deployOnTagRegex);
        final Long numberOfDaysBetweenStartDateAndEndDate =
                DateHelper.getNumberOfDaysBetweenStartDateAndEndDate(startDate, endDate);

        final List<TagView> tagsMatchingTeamIdAndDeployTagRegex =
                bffExpositionStorageAdapter.findTagsForTeamId(teamId);
        final List<String> commitsShaForTagsMatchingTeamIdAndDeployTagRegex =
                tagsMatchingTeamIdAndDeployTagRegex
                        .stream()
                        .filter(tag -> tagPattern.matcher(tag.getName()).find())
                        .map(TagView::getCommitSha)
                        .toList();
        final List<CommitView> currentCommitsMatchingTagRegexBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readCommitsMatchingShaListBetweenStartDateAndEndDate(commitsShaForTagsMatchingTeamIdAndDeployTagRegex,
                        startDate, endDate);
        final List<CommitView> previousCommitMatchingTagRegexBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readCommitsMatchingShaListBetweenStartDateAndEndDate(commitsShaForTagsMatchingTeamIdAndDeployTagRegex,
                        previousStartDate, startDate);

        final Optional<Deployment> optionalCurrentDeployment =
                deploymentService.buildForTagRegexSettings(
                        currentCommitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate,
                        tagsMatchingTeamIdAndDeployTagRegex);
        final Optional<Deployment> optionalPreviousDeployment =
                deploymentService.buildForTagRegexSettings(
                        previousCommitMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate,
                        tagsMatchingTeamIdAndDeployTagRegex);
        return DeploymentMetrics.buildFromCurrentAndPreviousDeployment(optionalCurrentDeployment,
                optionalPreviousDeployment, previousStartDate, startDate,
                endDate);
    }
}
