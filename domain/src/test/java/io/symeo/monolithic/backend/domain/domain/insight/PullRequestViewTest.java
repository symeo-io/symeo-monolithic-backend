package io.symeo.monolithic.backend.domain.domain.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        final PullRequestView pullRequestView1 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
                , null, null,
                PullRequest.OPEN);
        final PullRequestView pullRequestView2 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
                , stringToDate("2019-01-15"), null,
                PullRequest.OPEN);
        final PullRequestView pullRequestView3 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
                , stringToDate("2019-01-15"), stringToDate("2019-01-25"),
                PullRequest.OPEN);
        final PullRequestView pullRequestView4 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
                , null, stringToDate("2019-01-25"),
                PullRequest.OPEN);
        final PullRequestView pullRequestView5 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
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
        final PullRequestView pullRequestView1 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
                , null, null,
                PullRequest.OPEN);
        final PullRequestView pullRequestView2 = buildPullRequestPullRequestLimitView(2000f, stringToDate("2019-01-01")
                , stringToDate("2019-06-01"), null,
                PullRequest.OPEN);


        // Then
        assertThat(pullRequestView1.isInDateRange(stringToDate("2020-01-01"), stringToDate("2020-06-01"))).isTrue();
        assertThat(pullRequestView1.isInDateRange(stringToDate("2018-01-01"), stringToDate("2020-06-01"))).isTrue();
        assertThat(pullRequestView1.isInDateRange(stringToDate("2018-01-01"), stringToDate("2018-06-01"))).isFalse();
        assertThat(pullRequestView2.isInDateRange(stringToDate("2018-01-01"), stringToDate("2019-06-01"))).isTrue();

    }

    @Test
    void should_compute_number_of_days_opened_compared_to_end_date() throws ParseException {
        // Given
        final ZoneId zoneId = ZoneId.systemDefault();
        final Date creationDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-12 11:00:00").toInstant());
        final Date endDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-17 11:00:00").toInstant());

        final PullRequestView pullRequestView = buildPullRequestPullRequestLimitView(2000f, creationDate
                , null, null,
                PullRequest.OPEN);

        // When
        final float daysOpenedFromEndDate = pullRequestView.getDaysOpened(endDate);

        // Then
        assertThat(daysOpenedFromEndDate).isEqualTo(5f);
    }

    @Test
    void should_compute_number_of_days_opened_compared_to_end_date_with_merge_date() throws
            ParseException {
        // Given
        final ZoneId zoneId = ZoneId.systemDefault();
        final Date creationDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-12 11:00:00").toInstant());
        final Date mergeDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-14 11:00:00").toInstant());
        final Date endDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-17 11:00:00").toInstant());

        final PullRequestView pullRequestView = buildPullRequestPullRequestLimitView(2000f, creationDate
                , mergeDate, null,
                PullRequest.OPEN);

        // When
        final float daysOpenedFromEndDate = pullRequestView.getDaysOpened(endDate);

        // Then
        assertThat(daysOpenedFromEndDate).isEqualTo(2f);
    }


    @Test
    void should_compute_number_of_days_opened_for_less_than_hour() throws SymeoException, ParseException {
        // Given
        final ZoneId zoneId = ZoneId.systemDefault();
        final Date creationDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-12 11:00:00").toInstant());
        final Date endDate =
                Date.from(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2022-08-12 11:30:00").toInstant());

        final PullRequestView pullRequestView = buildPullRequestPullRequestLimitView(2000f, creationDate
                , null, null,
                PullRequest.OPEN);

        // When
        final float daysOpenedFromEndDate = pullRequestView.getDaysOpened(endDate);

        // Then
        assertThat(daysOpenedFromEndDate).isEqualTo(0.1f);
    }


    @Test
    void should_check_if_date_limit_is_above_limit() throws SymeoException {
        // Given
        final int range = 3;
        final PullRequestView pullRequestView1 = PullRequestView.builder()
                .creationDate(stringToDate("2022-03-01"))
                .build();
        final PullRequestView pullRequestView2 = PullRequestView.builder()
                .creationDate(stringToDate("2022-03-01"))
                .mergeDate(stringToDate("2022-03-03"))
                .build();


        // When
        final boolean aboveTimeLimit1 = pullRequestView1.isAboveTimeLimit(7, stringToDate("2022-04-1"), range);
        final boolean aboveTimeLimit2 = pullRequestView2.isAboveTimeLimit(7, stringToDate("2022-04-1"), range);
        // Then
        assertThat(aboveTimeLimit1).isTrue();
        assertThat(aboveTimeLimit2).isFalse();
    }

    @Test
    void should_contains_available_sorting_parameters() {
        // Then
        assertThat(PullRequestView.AVAILABLE_SORTING_PARAMETERS).isEqualTo(List.of(
                "status", "creation_date", "merge_date", "size", "days_opened", "id", "commit_number", "vcs_url",
                "title"
                , "author", "vcs_repository"
        ));
    }

    public static PullRequestView buildPullRequestPullRequestLimitView(final Float limit,
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
