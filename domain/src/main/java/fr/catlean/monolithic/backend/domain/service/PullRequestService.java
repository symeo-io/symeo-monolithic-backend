package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsTeam;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static fr.catlean.monolithic.backend.domain.helper.DateHelper.getWeekStartDateForTheLastWeekNumber;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.SIZE_LIMIT;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.TIME_LIMIT;

@AllArgsConstructor
public class PullRequestService {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public void computeAndSavePullRequestSizeHistogram(List<PullRequest> pullRequests,
                                                       Organization organizationAccount) {
        computeAndSavePullRequestHistogram(pullRequests, organizationAccount, SIZE_LIMIT);
    }

    public void computeAndSavePullRequestTimeHistogram(List<PullRequest> pullRequests,
                                                       Organization organizationAccount) {
        computeAndSavePullRequestHistogram(pullRequests, organizationAccount, TIME_LIMIT);
    }

    private void computeAndSavePullRequestHistogram(List<PullRequest> pullRequests,
                                                    Organization organizationAccount, String sizeLimit) {
        final List<PullRequestHistogram> pullRequestHistograms = new ArrayList<>();
        for (VcsTeam vcsTeam : organizationAccount.getVcsConfiguration().getVcsTeams()) {

            final PullRequestHistogram pullRequestHistogram =
                    getPullRequestHistogramForVcsTeam(sizeLimit, pullRequests,
                            organizationAccount, vcsTeam);
            pullRequestHistograms.add(pullRequestHistogram);

        }
        expositionStorageAdapter.savePullRequestHistograms(pullRequestHistograms);
    }


    private PullRequestHistogram getPullRequestHistogramForVcsTeam(final String pullRequestHistogramType,
                                                                   List<PullRequest> pullRequests,
                                                                   final Organization organizationAccount,
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
                pullRequests.stream().filter(PullRequest::isValid).toList();
        return pullRequests;
    }

    private DataCompareToLimit getDataCompareToLimit(final String pullRequestHistogramType,
                                                     final List<PullRequest> pullRequests, final int pullRequestLimit,
                                                     final Date weekStartDate, final String weekStart) {
        DataCompareToLimit dataCompareToLimit =
                DataCompareToLimit.builder().dateAsString(weekStart).build();

        for (PullRequest pullRequest : pullRequests) {
            if (pullRequest.isConsideredAsOpenDuringWeek(weekStartDate)) {
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
        if (pullRequest.isAboveSizeLimit(pullRequestLimit)) {
            dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        } else {
            dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        }
        return dataCompareToLimit;
    }

    private static DataCompareToLimit getDataCompareToTimeLimit(int pullRequestLimit, Date weekStartDate,
                                                                DataCompareToLimit dataCompareToLimit,
                                                                PullRequest pullRequest) {
        if (pullRequest.isAboveTimeLimit(pullRequestLimit, weekStartDate)) {
            dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        } else {
            dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        }
        return dataCompareToLimit;
    }


}
