package io.symeo.monolithic.backend.domain.model.insight.view;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;
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

    public PullRequestView addStartDateRangeFromRangeDates(final List<Date> rangeDates) {
        String startDateRange;
        if (isNull(mergeDate) && isNull(closeDate)) {
            startDateRange = dateToString(rangeDates.get(rangeDates.size() - 1));
        } else if (nonNull(mergeDate)) {
            Date closestRangeDate = rangeDates.get(0);
            closestRangeDate = getClosestRangeDate(rangeDates, mergeDate, closestRangeDate);
            startDateRange = dateToString(closestRangeDate);
        } else {
            Date closestRangeDate = rangeDates.get(0);
            closestRangeDate = getClosestRangeDate(rangeDates, closeDate, closestRangeDate);
            startDateRange = dateToString(closestRangeDate);
        }
        return this.toBuilder().startDateRange(startDateRange).build();
    }

    private Date getClosestRangeDate(List<Date> rangeDates, Date mergeDate, Date closestRangeDate) {
        for (Date rangeDate : rangeDates) {
            if (Math.abs(TimeUnit.DAYS.convert(mergeDate.getTime() - rangeDate.getTime(),
                    TimeUnit.MILLISECONDS)) < Math.abs(TimeUnit.DAYS.convert(mergeDate.getTime() - closestRangeDate.getTime(),
                    TimeUnit.MILLISECONDS))) {
                closestRangeDate = rangeDate;
            }
        }
        return closestRangeDate;
    }

    public boolean isConsideredOpenedForRangeStartDate(final Date rangeStartDate, final int range) {
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

    public boolean isInDateRange(final Date startDate, final Date endDate) {
        final Date creationDate = this.getCreationDate();
        final Date mergeDate = this.getMergeDate();
        final Date closeDate = this.getCloseDate();
        if (creationDate.after(endDate)) {
            return false;
        }
        if (isNull(mergeDate) && isNull(closeDate)) {
            if (creationDate.before(endDate)) {
                return true;
            }
        } else if (isNull(closeDate)) {
            if (mergeDate.after(startDate)) {
                return true;
            }
        } else {
            if (closeDate.after(startDate)) {
                return true;
            }
        }
        return false;
    }
}
