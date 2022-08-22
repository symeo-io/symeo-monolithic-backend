package io.symeo.monolithic.backend.domain.domain.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestViewTest {

    @Test
    void should_add_range_date_to_pull_request_view() throws SymeoException {
        // Given
        final PullRequestView pullRequestView1 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , null, null,
                PullRequest.OPEN);
        final PullRequestView pullRequestView2 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , stringToDate("2019-01-15"), null,
                PullRequest.OPEN);
        final PullRequestView pullRequestView3 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , stringToDate("2019-01-15"), stringToDate("2019-01-25"),
                PullRequest.OPEN);
        final PullRequestView pullRequestView4 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , null, stringToDate("2019-01-25"),
                PullRequest.OPEN);
        final PullRequestView pullRequestView5 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , stringToDate("2020-01-15"), null,
                PullRequest.OPEN);


        final int range = 5;
        final String endDate = "2019-02-01";
        final List<Date> rangeDates =
                DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(
                        stringToDate("2019-01-01"), stringToDate(endDate), range,
                        TimeZone.getTimeZone(ZoneId.systemDefault()));

        // When
        final PullRequestView pullRequestViewUpdated1 = pullRequestView1.addStartDateRangeFromRangeDates(rangeDates);
        final PullRequestView pullRequestViewUpdated2 = pullRequestView2.addStartDateRangeFromRangeDates(rangeDates);
        final PullRequestView pullRequestViewUpdated3 = pullRequestView3.addStartDateRangeFromRangeDates(rangeDates);
        final PullRequestView pullRequestViewUpdated4 = pullRequestView4.addStartDateRangeFromRangeDates(rangeDates);
        final PullRequestView pullRequestViewUpdated5 = pullRequestView5.addStartDateRangeFromRangeDates(rangeDates);


        // Then
        assertThat(pullRequestViewUpdated1.getStartDateRange()).isEqualTo(endDate);
        assertThat(pullRequestViewUpdated2.getStartDateRange()).isEqualTo("2019-01-16");
        assertThat(pullRequestViewUpdated3.getStartDateRange()).isEqualTo("2019-01-16");
        assertThat(pullRequestViewUpdated4.getStartDateRange()).isEqualTo("2019-01-26");
        assertThat(pullRequestViewUpdated5.getStartDateRange()).isEqualTo(endDate);
    }


    @Test
    void should_check_is_in_date_range() throws SymeoException {
        // Given
        final PullRequestView pullRequestView1 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , null, null,
                PullRequest.OPEN);
        final PullRequestView pullRequestView2 = buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01")
                , stringToDate("2019-06-01"), null,
                PullRequest.OPEN);


        // Then
        assertThat(pullRequestView1.isInDateRange(stringToDate("2020-01-01"), stringToDate("2020-06-01"))).isTrue();
        assertThat(pullRequestView1.isInDateRange(stringToDate("2018-01-01"), stringToDate("2020-06-01"))).isTrue();
        assertThat(pullRequestView1.isInDateRange(stringToDate("2018-01-01"), stringToDate("2018-06-01"))).isFalse();
        assertThat(pullRequestView2.isInDateRange(stringToDate("2018-01-01"), stringToDate("2019-06-01"))).isTrue();

    }

    @Test
    void should_compute_number_of_days_opened_compared_to_end_date() throws SymeoException {
        // Given
        final ZoneId zoneId = ZoneId.systemDefault();
        final Date creationDate =
                Date.from((new Date().toInstant().atZone(zoneId).toLocalDate().minusDays(10)).atStartOfDay(zoneId).toInstant());
        final Date endDate =
                Date.from((new Date().toInstant().atZone(zoneId).toLocalDate().minusDays(5)).atStartOfDay(zoneId).toInstant());

        final PullRequestView pullRequestView = buildPullRequestPullRequestLimitView(2000, creationDate
                , null, null,
                PullRequest.OPEN);

        // When
        final int daysOpened = pullRequestView.getDaysOpened(new Date());
        final int daysOpenedFromEndDate = pullRequestView.getDaysOpened(endDate);

        // Then
        assertThat(daysOpened).isEqualTo(10);
        assertThat(daysOpenedFromEndDate).isEqualTo(5);
    }

    public static PullRequestView buildPullRequestPullRequestLimitView(final Integer limit,
                                                                       final Date creationDate,
                                                                       final Date mergeDate,
                                                                       final Date closeDate,
                                                                       final String status) {
        return PullRequestView.builder()
                .limit(limit)
                .creationDate(creationDate)
                .closeDate(closeDate)
                .mergeDate(mergeDate)
                .status(status)
                .build();
    }
}
