package io.symeo.monolithic.backend.domain.model.insight.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;
import static java.lang.Math.round;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class PullRequestView {
    String status;
    Date creationDate;
    Date mergeDate;
    Date closeDate;
    String startDateRange;
    Integer deletedLineNumber;
    Integer addedLineNumber;
    Float limit;
    String branchName;
    String vcsUrl;
    String authorLogin;
    Integer commitNumber;
    String id;
    String title;
    String repository;

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

    public boolean isAboveTimeLimit(final int pullRequestLimit, final Date weekStartDate) {
        final Date creationDate = this.getCreationDate();
        final Date mergeDate = this.getMergeDate();
        if (isNull(mergeDate) || (mergeDate.after(weekStartDate))) {
            long daysBetweenCreationAndWeekStart =
                    TimeUnit.DAYS.convert(weekStartDate.getTime() - creationDate.getTime(),
                            TimeUnit.MILLISECONDS);
            return daysBetweenCreationAndWeekStart > pullRequestLimit;
        } else {
            long daysBetweenMergeAndWeekStart =
                    TimeUnit.DAYS.convert(weekStartDate.getTime() - mergeDate.getTime(),
                            TimeUnit.MILLISECONDS);
            return daysBetweenMergeAndWeekStart > pullRequestLimit;
        }
    }

    public boolean isAboveSizeLimit(int limit) {
        return this.getSize() >= limit;
    }

    public float getSize() {
        final int size = this.addedLineNumber + this.deletedLineNumber;
        return size == 0f ? 1f : size;
    }

    public boolean isInDateRange(final Date startDate, final Date endDate) {
        final Date creationDate = this.getCreationDate();
        final Date mergeDate = this.getMergeDate();
        final Date closeDate = this.getCloseDate();
        if (creationDate.after(endDate)) {
            return false;
        }
        if (isNull(mergeDate) && isNull(closeDate)) {
            return creationDate.before(endDate);
        } else if (isNull(closeDate)) {
            return mergeDate.after(startDate);
        } else {
            return closeDate.after(startDate);
        }
    }

    public float getDaysOpened(final Date endDate) {
        if (isNull(this.mergeDate) && isNull(this.closeDate)) {
            return hoursToDays(TimeUnit.HOURS.convert(endDate.getTime() - creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        if (isNull(this.mergeDate)) {
            return hoursToDays(TimeUnit.HOURS.convert(closeDate.getTime() - this.creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        return hoursToDays(TimeUnit.HOURS.convert(mergeDate.getTime() - this.creationDate.getTime(),
                TimeUnit.MILLISECONDS));
    }

    private static float hoursToDays(long hours) {
        return hours < 2f ? 0.1f : round(10f * hours / 24) / 10f;
    }

    public PullRequestView addTimeLimit() {
        return this.toBuilder().limit(this.getDaysOpened(new Date())).build();
    }

    public PullRequestView addSizeLimit() {
        return this.toBuilder().limit(this.getSize()).build();
    }
}
