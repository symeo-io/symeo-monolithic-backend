package fr.catlean.monolithic.backend.domain.model.insight.view;

import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Builder(toBuilder = true)
@Data
public class PullRequestView {
    String status;
    Date creationDate;
    Date mergeDate;
    Date closeDate;
    String startDateRange;
    Integer limit;

    public PullRequestView addStartDateRangeFromRangeDates(final List<Date> rangeDates, final int range) {
        return this.toBuilder()
                .startDateRange(
                        DateHelper.dateToString(
                                rangeDates.stream().filter(date -> isConsideredForRangeStartDate(date, range))
                                        .findFirst()
                                        .orElseGet(() -> rangeDates.get(rangeDates.size() - 1))
                        )
                )
                .build();
    }

    public String getStartDateRange() {
        if (isNull(this.mergeDate) && isNull(this.closeDate)) {
            return DateHelper.dateToString(new Date());
        }
        if (nonNull(this.mergeDate)) {
            return DateHelper.dateToString(this.mergeDate);
        }
        return DateHelper.dateToString(this.closeDate);
    }

    public boolean isConsideredForRangeStartDate(final Date rangeStartDate, final int range) {
        final Date creationDate = this.getCreationDate();
        final Date mergeDate = this.getMergeDate();
        if (creationDate.before(rangeStartDate)) {
            return isNull(mergeDate) || mergeDate.after(rangeStartDate);
        } else {
            if (creationDate.equals(rangeStartDate)) {
                return true;
            } else if (nonNull(mergeDate)) {
                long daysBetweenMergeAndRangeStart =
                        TimeUnit.DAYS.convert(mergeDate.getTime() - rangeStartDate.getTime(),
                                TimeUnit.MILLISECONDS);
                long daysBetweenCreationAndRangeStart =
                        TimeUnit.DAYS.convert(creationDate.getTime() - rangeStartDate.getTime(),
                                TimeUnit.MILLISECONDS);
                return daysBetweenCreationAndRangeStart < range && daysBetweenMergeAndRangeStart < range;
            }
        }
        return false;
    }

    public boolean isAboveTimeLimit(int pullRequestLimit, Date weekStartDate) {
        final Date creationDate = this.getCreationDate();
        final Date mergeDate = this.getMergeDate();
        if (isNull(mergeDate) || (mergeDate.after(weekStartDate))) {
            long daysBetweenCreationAndWeekStart =
                    TimeUnit.DAYS.convert(creationDate.getTime() - weekStartDate.getTime(),
                            TimeUnit.MILLISECONDS);
            return Math.abs(daysBetweenCreationAndWeekStart) > pullRequestLimit;
        } else {
            long daysBetweenMergeAndWeekStart =
                    TimeUnit.DAYS.convert(mergeDate.getTime() - weekStartDate.getTime(),
                            TimeUnit.MILLISECONDS);
            return Math.abs(daysBetweenMergeAndWeekStart) > pullRequestLimit;
        }
    }

    public boolean isAboveSizeLimit(int limit) {
        return this.getLimit() >= limit;
    }
}
