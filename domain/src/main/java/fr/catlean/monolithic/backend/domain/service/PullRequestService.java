package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.model.account.VcsTeam;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorage;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static fr.catlean.monolithic.backend.domain.helper.DateHelper.getWeekStartDateForTheLastWeekNumber;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.SIZE_LIMIT;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.TIME_LIMIT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class PullRequestService {

    private final ExpositionStorage expositionStorage;
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public void computeAndSavePullRequestSizeHistogram(List<PullRequest> pullRequests,
                                                       OrganizationAccount organizationAccount) {
        final List<PullRequestHistogram> pullRequestHistograms = new ArrayList<>();
        for (VcsTeam vcsTeam : organizationAccount.getVcsConfiguration().getVcsTeams()) {

            final PullRequestHistogram pullRequestHistogram =
                    getPullRequestHistogramForVcsTeam(SIZE_LIMIT, pullRequests,
                            organizationAccount, vcsTeam);
            pullRequestHistograms.add(pullRequestHistogram);

        }
        expositionStorage.savePullRequestHistograms(pullRequestHistograms);
    }

    public void computeAndSavePullRequestTimeHistogram(List<PullRequest> pullRequests,
                                                       OrganizationAccount organizationAccount) {
        final List<PullRequestHistogram> pullRequestHistograms = new ArrayList<>();
        for (VcsTeam vcsTeam : organizationAccount.getVcsConfiguration().getVcsTeams()) {

            final PullRequestHistogram pullRequestHistogram =
                    getPullRequestHistogramForVcsTeam(TIME_LIMIT, pullRequests,
                            organizationAccount, vcsTeam);
            pullRequestHistograms.add(pullRequestHistogram);

        }
        expositionStorage.savePullRequestHistograms(pullRequestHistograms);
    }


    private PullRequestHistogram getPullRequestHistogramForVcsTeam(final String pullRequestHistogramType,
                                                                   List<PullRequest> pullRequests,
                                                                   final OrganizationAccount organizationAccount,
                                                                   final VcsTeam vcsTeam) {
        int pullRequestLimit;
        if (pullRequestHistogramType.equals(SIZE_LIMIT)) {
            pullRequestLimit = vcsTeam.getPullRequestLineNumberLimit();
        } else {
            pullRequestLimit = vcsTeam.getPullRequestDayNumberLimit();
        }

        pullRequests = filterPullRequests(pullRequests, vcsTeam);
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        for (Date weekStartDate : getWeekStartDateForTheLastWeekNumber(3 * 4,
                organizationAccount.getTimeZone())) {
            final String weekStart = SDF.format(weekStartDate);
            dataCompareToLimits.add(getDataCompareToLimit(pullRequestHistogramType, pullRequests, pullRequestLimit,
                    weekStartDate, weekStart));
        }
        return PullRequestHistogram.builder()
                .type(pullRequestHistogramType)
                .organizationAccount(organizationAccount.getName())
                .team(vcsTeam.getName())
                .dataByWeek(dataCompareToLimits)
                .limit(pullRequestLimit)
                .build();

    }

    private static List<PullRequest> filterPullRequests(List<PullRequest> pullRequests, VcsTeam vcsTeam) {
        pullRequests =
                pullRequests.stream()
                        .filter(pullRequest -> vcsTeam.getVcsRepositoryNames().stream()
                                .anyMatch(repositoryName -> pullRequest.getRepository().equals(repositoryName))
                        ).toList();
        pullRequests =
                // TODO: remove the filter on the date and add condition on business rules
                pullRequests.stream().filter(pullRequest -> !pullRequest.getIsDraft() && !(isNull(pullRequest.getMergeDate()) && nonNull(pullRequest.getCloseDate()))).toList();
        return pullRequests;
    }

    private DataCompareToLimit getDataCompareToLimit(final String pullRequestHistogramType,
                                                     final List<PullRequest> pullRequests, final int pullRequestLimit,
                                                     final Date weekStartDate, final String weekStart) {
        DataCompareToLimit dataCompareToLimit =
                DataCompareToLimit.builder().dateAsString(weekStart).build();

        for (PullRequest pullRequest : pullRequests) {
            if (pullRequestIsConsideredAsOpenDuringWeek(pullRequest, weekStartDate)) {
                if (pullRequestHistogramType.equals(SIZE_LIMIT)) {
                    dataCompareToLimit = getDataCompareToSizeLimit(pullRequestLimit, dataCompareToLimit, pullRequest);
                } else {
                    dataCompareToLimit = getDataCompareToTimeLimit(pullRequestLimit, weekStartDate,
                            dataCompareToLimit, pullRequest);
                }
            }
        }
        return dataCompareToLimit;
    }

    private static DataCompareToLimit getDataCompareToSizeLimit(int pullRequestLimit,
                                                                DataCompareToLimit dataCompareToLimit,
                                                                PullRequest pullRequest) {
        if (pullRequest.getAddedLineNumber() + pullRequest.getDeletedLineNumber() >= pullRequestLimit) {
            dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        } else {
            dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        }
        return dataCompareToLimit;
    }

    private static DataCompareToLimit getDataCompareToTimeLimit(int pullRequestLimit, Date weekStartDate,
                                                                DataCompareToLimit dataCompareToLimit,
                                                                PullRequest pullRequest) {
        final Date creationDate = pullRequest.getCreationDate();
        final Date mergeDate = pullRequest.getMergeDate();
        if (isNull(mergeDate) || (mergeDate.after(weekStartDate))) {
            long daysBetweenCreationAndWeekStart =
                    TimeUnit.DAYS.convert(creationDate.getTime() - weekStartDate.getTime(),
                            TimeUnit.MILLISECONDS);
            if (Math.abs(daysBetweenCreationAndWeekStart) > pullRequestLimit) {
                dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
            } else {
                dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
            }
        } else {
            long daysBetweenMergeAndWeekStart =
                    TimeUnit.DAYS.convert(mergeDate.getTime() - weekStartDate.getTime(),
                            TimeUnit.MILLISECONDS);
            if (Math.abs(daysBetweenMergeAndWeekStart) > pullRequestLimit) {
                dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
            } else {
                dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
            }
        }
        return dataCompareToLimit;
    }

    private boolean pullRequestIsConsideredAsOpenDuringWeek(final PullRequest pullRequest, final Date weekStartDate) {
        final Date creationDate = pullRequest.getCreationDate();
        final Date mergeDate = pullRequest.getMergeDate();
        if (creationDate.before(weekStartDate)) {
            return isNull(mergeDate) || mergeDate.after(weekStartDate);
        } else {
            if (creationDate.equals(weekStartDate)) {
                return true;
            } else if (nonNull(mergeDate)) {
                long daysBetweenMergeAndWeekStart =
                        TimeUnit.DAYS.convert(mergeDate.getTime() - weekStartDate.getTime(),
                                TimeUnit.MILLISECONDS);
                long daysBetweenCreationAndWeekStart =
                        TimeUnit.DAYS.convert(creationDate.getTime() - weekStartDate.getTime(),
                                TimeUnit.MILLISECONDS);
                return daysBetweenCreationAndWeekStart < 7 && daysBetweenMergeAndWeekStart < 7;
            }
        }
        return false;
    }


}
