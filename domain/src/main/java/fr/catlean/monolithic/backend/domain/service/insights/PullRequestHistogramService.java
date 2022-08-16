package fr.catlean.monolithic.backend.domain.service.insights;

import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestView;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.SIZE_LIMIT;

@AllArgsConstructor
public class PullRequestHistogramService {

    public PullRequestHistogram getPullRequestHistogram(final String pullRequestHistogramType,
                                                        final List<PullRequestView> pullRequestViews,
                                                        final Organization organization,
                                                        final TeamGoal teamGoal,
                                                        final List<Date> rangeDates,
                                                        final int range) {
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        for (Date rangeStartDate : rangeDates) {
            dataCompareToLimits.add(getDataCompareToLimit(pullRequestHistogramType, pullRequestViews,
                    teamGoal.getValueAsInteger(),
                    rangeStartDate, range));
        }
        return PullRequestHistogram.builder()
                .type(pullRequestHistogramType)
                .organizationId(organization.getId())
                .dataByWeek(dataCompareToLimits)
                .limit(teamGoal.getValueAsInteger())
                .build();
    }

    private static DataCompareToLimit getDataCompareToLimit(final String pullRequestHistogramType,
                                                            final List<PullRequestView> pullRequestViews,
                                                            final int pullRequestLimit,
                                                            final Date rangeStartDate,
                                                            final int range) {
        DataCompareToLimit dataCompareToLimit =
                DataCompareToLimit.builder().dateAsString(DateHelper.dateToString(rangeStartDate)).build();
        for (PullRequestView pullRequestView : pullRequestViews) {
            if (pullRequestView.isConsideredForRangeStartDate(rangeStartDate, range)) {
                if (pullRequestHistogramType.equals(SIZE_LIMIT)) {
                    dataCompareToLimit = getDataCompareToSizeLimit(pullRequestLimit, dataCompareToLimit,
                            pullRequestView);
                } else {
                    dataCompareToLimit = getDataCompareToTimeLimit(pullRequestLimit, rangeStartDate,
                            dataCompareToLimit, pullRequestView);
                }
            }
        }
        return dataCompareToLimit;
    }

    private static DataCompareToLimit getDataCompareToSizeLimit(int pullRequestLimit,
                                                                DataCompareToLimit dataCompareToLimit,
                                                                PullRequestView pullRequestView) {
        if (pullRequestView.isAboveSizeLimit(pullRequestLimit)) {
            dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        } else {
            dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        }
        return dataCompareToLimit;
    }

    private static DataCompareToLimit getDataCompareToTimeLimit(int pullRequestLimit, Date weekStartDate,
                                                                DataCompareToLimit dataCompareToLimit,
                                                                PullRequestView pullRequestView) {
        if (pullRequestView.isAboveTimeLimit(pullRequestLimit, weekStartDate)) {
            dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        } else {
            dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        }
        return dataCompareToLimit;
    }

}
