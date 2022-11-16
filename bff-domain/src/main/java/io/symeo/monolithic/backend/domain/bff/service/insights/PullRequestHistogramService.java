package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.model.metric.DataCompareToLimit;
import io.symeo.monolithic.backend.domain.bff.model.metric.PullRequestHistogram;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            if (pullRequestView.isConsideredOpenedForRangeStartDate(rangeStartDate, range)) {
                if (pullRequestHistogramType.equals(PullRequestHistogram.SIZE_LIMIT)) {
                    dataCompareToLimit = getDataCompareToSizeLimit(pullRequestLimit, dataCompareToLimit,
                            pullRequestView);
                } else {
                    dataCompareToLimit = getDataCompareToTimeLimit(pullRequestLimit, rangeStartDate,
                            dataCompareToLimit, pullRequestView, range);
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

    private static DataCompareToLimit getDataCompareToTimeLimit(final int pullRequestLimit, final Date weekStartDate,
                                                                DataCompareToLimit dataCompareToLimit,
                                                                final PullRequestView pullRequestView,
                                                                final int range) {
        if (pullRequestView.isAboveTimeLimit(pullRequestLimit, weekStartDate, range)) {
            dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        } else {
            dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        }
        return dataCompareToLimit;
    }

}
