package fr.catlean.monolithic.backend.domain.service.insights;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
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
public class PullRequestHistogramService {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final static SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public void computeAndSavePullRequestSizeHistogram(List<PullRequest> pullRequests,
                                                       Organization organization) {
        computeAndSavePullRequestHistogram(pullRequests, organization, SIZE_LIMIT);
    }

    public void computeAndSavePullRequestTimeHistogram(List<PullRequest> pullRequests,
                                                       Organization organization) {
        computeAndSavePullRequestHistogram(pullRequests, organization, TIME_LIMIT);
    }

    private void computeAndSavePullRequestHistogram(List<PullRequest> pullRequests,
                                                    Organization organization, String pullRequestHistogramType) {
        final List<PullRequestHistogram> pullRequestHistograms = new ArrayList<>();

        for (Team team : organization.getTeams()) {
            final PullRequestHistogram pullRequestHistogram =
                    getPullRequestHistogramsForVcsTeam(pullRequestHistogramType, pullRequests,
                            organization, team);
            pullRequestHistograms.add(pullRequestHistogram);

        }
        final Team teamAll = Team.buildTeamAll(organization.getId());
        pullRequestHistograms.add(getPullRequestHistogram(pullRequestHistogramType, pullRequests, organization, teamAll,
                teamAll.getPullRequestLineNumberLimit()));
        expositionStorageAdapter.savePullRequestHistograms(pullRequestHistograms);
    }


    private PullRequestHistogram getPullRequestHistogramsForVcsTeam(final String pullRequestHistogramType,
                                                                    List<PullRequest> pullRequests,
                                                                    final Organization organization,
                                                                    final Team team) {
        int pullRequestLimit;
        if (pullRequestHistogramType.equals(SIZE_LIMIT)) {
            pullRequestLimit = team.getPullRequestLineNumberLimit();
        } else {
            pullRequestLimit = team.getPullRequestDayNumberLimit();
        }

        pullRequests = filterPullRequests(pullRequests, team);
        return getPullRequestHistogram(pullRequestHistogramType, pullRequests, organization, team,
                pullRequestLimit);

    }

    public static PullRequestHistogram getPullRequestHistogram(String pullRequestHistogramType,
                                                         List<PullRequest> pullRequests, Organization organization,
                                                         Team team, int pullRequestLimit) {
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        for (Date weekStartDate : getWeekStartDateForTheLastWeekNumber(3 * 4,
                organization.getTimeZone())) {
            final String weekStart = SDF.format(weekStartDate);
            dataCompareToLimits.add(getDataCompareToLimit(pullRequestHistogramType, pullRequests, pullRequestLimit,
                    weekStartDate, weekStart));
        }
        return PullRequestHistogram.builder()
                .type(pullRequestHistogramType)
                .organizationId(organization.getId())
                .team(team.getName())
                .dataByWeek(dataCompareToLimits)
                .limit(pullRequestLimit)
                .build();
    }

    private static List<PullRequest> filterPullRequests(List<PullRequest> pullRequests, Team team) {
        pullRequests =
                pullRequests.stream()
                        .filter(pullRequest -> team.getRepositories().stream()
                                .anyMatch(repository -> pullRequest.getRepository().equals(repository.getName()))
                        ).toList();
        pullRequests =
                pullRequests.stream().filter(PullRequest::isValid).toList();
        return pullRequests;
    }

    private static DataCompareToLimit getDataCompareToLimit(final String pullRequestHistogramType,
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


    public void savePullRequests(List<PullRequest> pullRequests) {
        expositionStorageAdapter.savePullRequestDetails(pullRequests);
    }
}
