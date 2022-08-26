package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.LeadTime;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.in.LeadTimeFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;

@AllArgsConstructor
public class LeadTimeService implements LeadTimeFacadeAdapter {

    private final ExpositionStorageAdapter expositionStorageAdapter;

    @Override
    public LeadTimeMetrics computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                 final UUID teamId,
                                                                                 final Date startDate,
                                                                                 final Date endDate) {
        final List<PullRequestView> currentPullRequestWithCommitsViews =
                expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId, startDate
                        , endDate);
        final LeadTime currentLeadTime =
                LeadTime.buildFromPullRequestWithCommitsViews(currentPullRequestWithCommitsViews);
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate,
                endDate, organization.getTimeZone());
        final List<PullRequestView> previousPullRequestWithCommitsViews =
                expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                        previousStartDate
                        , startDate);
        final LeadTime previousLeadTime =
                LeadTime.buildFromPullRequestWithCommitsViews(previousPullRequestWithCommitsViews);
        return LeadTimeMetrics.buildFromCurrentAndPreviousLeadTimes(currentLeadTime, previousLeadTime);
    }
}
