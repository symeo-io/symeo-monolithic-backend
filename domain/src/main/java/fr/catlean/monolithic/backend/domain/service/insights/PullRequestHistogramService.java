package fr.catlean.monolithic.backend.domain.service.insights;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
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

@AllArgsConstructor
public class PullRequestHistogramService {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final static SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public PullRequestHistogram getPullRequestHistogram(String pullRequestHistogramType,
                                                        List<PullRequest> pullRequests,
                                                        Organization organization,
                                                        TeamGoal teamGoal) {
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        for (Date weekStartDate : getWeekStartDateForTheLastWeekNumber(3 * 4,
                organization.getTimeZone())) {
            final String weekStart = SDF.format(weekStartDate);
            dataCompareToLimits.add(getDataCompareToLimit(pullRequestHistogramType, pullRequests,
                    teamGoal.getValueAsInteger(),
                    weekStartDate, weekStart));
        }
        return PullRequestHistogram.builder()
                .type(pullRequestHistogramType)
                .organizationId(organization.getId())
                .dataByWeek(dataCompareToLimits)
                .limit(teamGoal.getValueAsInteger())
                .build();
    }

    private static DataCompareToLimit getDataCompareToLimit(final String pullRequestHistogramType,
                                                            final List<PullRequest> pullRequests,
                                                            final int pullRequestLimit,
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
