package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiecePage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeMetricsFacadeAdapter;
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
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;

@Slf4j
@AllArgsConstructor
public class CycleTimeMetricsService implements CycleTimeMetricsFacadeAdapter {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
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
        final DeployDetectionTypeDomainEnum deployDetectionType =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType();
        final List<String> excludeBranchRegexes =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();
        if (!isNull(deployDetectionType) && deployDetectionType.equals(DeployDetectionTypeDomainEnum.PULL_REQUEST)) {
            return getCycleTimeMetricsForPullRequestMergedOnBranchRegex(teamId, startDate, endDate, previousStartDate,
                    pullRequestMergedOnBranchRegex, excludeBranchRegexes);
        } else if (!isNull(deployDetectionType) && deployDetectionType.equals(DeployDetectionTypeDomainEnum.TAG)) {
            return getCycleTimeMetricsForDeployOnTagRegex(teamId, startDate, endDate, previousStartDate, tagRegex,
                    excludeBranchRegexes);
        }
        LOGGER.warn("CycleTimeMetrics not computed due to missing delivery settings for organization {} and teamId {}" +
                        " " +
                        "and organizationSettings {}",
                organization, teamId, organizationSettings);
        return empty();
    }

    @Override
    public CycleTimePiecePage computeCycleTimePiecesForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                    final UUID teamId,
                                                                                    final Date startDate,
                                                                                    final Date endDate,
                                                                                    final Integer pageIndex,
                                                                                    final Integer pageSize,
                                                                                    final String sortBy,
                                                                                    final String sortDir) throws SymeoException {
        final OrganizationSettings organizationSettings =
                organizationSettingsFacade.getOrganizationSettingsForOrganization(organization);
        final DeployDetectionTypeDomainEnum deployDetectionType =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType();
        final List<String> excludeBranchRegexes =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        final String pullRequestMergedOnBranchRegex =
                organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();
        if (!isNull(deployDetectionType) && deployDetectionType.equals(DeployDetectionTypeDomainEnum.PULL_REQUEST)) {
            return getCycleTimePiecesForPullRequestMergedOnBranchRegex(teamId, startDate, endDate, pageIndex,
                    pageSize, sortBy, sortDir,
                    pullRequestMergedOnBranchRegex, excludeBranchRegexes);
        } else if (!isNull(deployDetectionType) && deployDetectionType.equals(DeployDetectionTypeDomainEnum.TAG)) {
            return getCycleTimePiecesForDeployOnTagRegex(teamId, startDate, endDate, pageIndex, pageSize, sortBy,
                    sortDir,
                    tagRegex, excludeBranchRegexes);
        }
        LOGGER.warn("CycleTimePieces not computed due to missing delivery settings for organization {} and teamId {} " +
                        "and organizationSettings {}",
                organization, teamId, organizationSettings);
        return CycleTimePiecePage.builder().build();
    }

    private Optional<CycleTimeMetrics> getCycleTimeMetricsForPullRequestMergedOnBranchRegex(final UUID teamId,
                                                                                            final Date startDate,
                                                                                            final Date endDate,
                                                                                            final Date previousStartDate,
                                                                                            final String pullRequestMergedOnBranchRegex,
                                                                                            final List<String> excludeBranchRegexes) throws SymeoException {
        final List<PullRequestView> currentPullRequestViews =
                bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                                endDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .collect(Collectors.toList());
        final List<PullRequestView> previousPullRequestViews =
                bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .toList();
        final List<CommitView> allCommitsUntilEndDate =
                bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                startDate, endDate)
                        .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

        final Optional<AverageCycleTime> currentCycleTime =
                cycleTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                        currentPullRequestViews,
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        allCommitsUntilEndDate
                );


        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                previousStartDate, startDate)
                        .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

        final Optional<AverageCycleTime> previousCycleTime =
                cycleTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                        previousPullRequestViews,
                        previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        allCommitsUntilEndDate
                );

        return CycleTimeMetrics.buildFromCurrentAndPreviousCycleTimes(currentCycleTime, previousCycleTime,
                previousStartDate, startDate,
                endDate);
    }

    private Optional<CycleTimeMetrics> getCycleTimeMetricsForDeployOnTagRegex(final UUID teamId,
                                                                              final Date startDate,
                                                                              final Date endDate,
                                                                              final Date previousStartDate,
                                                                              final String deployOnTagRegex,
                                                                              final List<String> excludeBranchRegexes) throws SymeoException {
        final List<PullRequestView> currentPullRequestViews =
                bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                                endDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .collect(Collectors.toList());
        final List<PullRequestView> previousPullRequestViews =
                bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes))
                        .toList();
        final List<CommitView> allCommitsUntilEndDate =
                bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern tagPattern = Pattern.compile(deployOnTagRegex);

        final List<TagView> tagsMatchingDeployTagRegex =
                bffExpositionStorageAdapter.findTagsForTeamId(teamId)
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
        return CycleTimeMetrics.buildFromCurrentAndPreviousCycleTimes(currentCycleTime, previousCycleTime,
                previousStartDate, startDate,
                endDate);
    }

    private CycleTimePiecePage getCycleTimePiecesForPullRequestMergedOnBranchRegex(final UUID teamId,
                                                                                   final Date startDate,
                                                                                   final Date endDate,
                                                                                   final Integer pageIndex,
                                                                                   final Integer pageSize,
                                                                                   final String sortBy,
                                                                                   final String sortDir,
                                                                                   final String pullRequestMergedOnBranchRegex,
                                                                                   final List<String> excludeBranchRegexes) throws SymeoException {
        validatePagination(pageIndex, pageSize);
        validateSortingInputs(sortDir, sortBy, PullRequestView.AVAILABLE_SORTING_PARAMETERS);
        final int totalNumberOfRequestViewsForTeamIdAndStartDateAndEndDate =
                bffExpositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                        startDate, endDate);
        final List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted =
                bffExpositionStorageAdapter.findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(
                                teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes)).toList();
        final List<CommitView> allCommitsUntilEndDate =
                bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                                startDate, endDate)
                        .stream().filter(pullRequestView -> branchPattern.matcher(pullRequestView.getBase()).find()).toList();

        final List<CycleTimePiece> cycleTimePiecesForPage =
                cycleTimeService.buildCycleTimePiecesForPullRequestsMergedOnBranchRegexSettings(
                        pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted,
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        allCommitsUntilEndDate);

        return CycleTimePiecePage.builder()
                .totalNumberOfPieces(totalNumberOfRequestViewsForTeamIdAndStartDateAndEndDate)
                .totalNumberOfPages((int) Math.ceil(1.0f * totalNumberOfRequestViewsForTeamIdAndStartDateAndEndDate / pageSize))
                .cycleTimePieces(cycleTimePiecesForPage)
                .build();
    }

    private CycleTimePiecePage getCycleTimePiecesForDeployOnTagRegex(final UUID teamId,
                                                                     final Date startDate,
                                                                     final Date endDate,
                                                                     final Integer pageIndex,
                                                                     final Integer pageSize,
                                                                     final String sortBy,
                                                                     final String sortDir,
                                                                     final String deployOnTagRegex,
                                                                     final List<String> excludeBranchRegexes) throws SymeoException {
        validatePagination(pageIndex, pageSize);
        validateSortingInputs(sortDir, sortBy, PullRequestView.AVAILABLE_SORTING_PARAMETERS);
        final int totalNumberOfRequestViewsForTeamIdAndStartDateAndEndDate =
                bffExpositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                        startDate, endDate);
        final List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted =
                bffExpositionStorageAdapter.findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(
                                teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir)
                        .stream()
                        .filter(pullRequestView -> excludePullRequest(pullRequestView, excludeBranchRegexes)).toList();
        final List<CommitView> allCommitsUntilEndDate =
                bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId);
        final Pattern tagPattern = Pattern.compile(deployOnTagRegex);

        final List<TagView> tagsMatchingDeployTagRegex =
                bffExpositionStorageAdapter.findTagsForTeamId(teamId)
                        .stream()
                        .filter(tag -> tagPattern.matcher(tag.getName()).find())
                        .toList();

        final List<CycleTimePiece> cycleTimePiecesForPage = cycleTimeService.buildCycleTimePiecesForTagRegexSettings(
                pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted,
                tagsMatchingDeployTagRegex,
                allCommitsUntilEndDate
        );

        return CycleTimePiecePage.builder()
                .totalNumberOfPieces(totalNumberOfRequestViewsForTeamIdAndStartDateAndEndDate)
                .totalNumberOfPages((int) Math.ceil(1.0f * totalNumberOfRequestViewsForTeamIdAndStartDateAndEndDate / pageSize))
                .cycleTimePieces(cycleTimePiecesForPage)
                .build();
    }

    private Boolean excludePullRequest(final PullRequestView pullRequestView, final List<String> excludeBranchRegexes) {
        return excludeBranchRegexes.isEmpty() || excludeBranchRegexes.stream().noneMatch(
                regex -> Pattern.compile(regex).matcher(pullRequestView.getHead()).find()
        );
    }
}
