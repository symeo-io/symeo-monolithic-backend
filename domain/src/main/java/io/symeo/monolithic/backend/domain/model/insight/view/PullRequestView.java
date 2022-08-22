package io.symeo.monolithic.backend.domain.model.insight.view;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;
import static java.lang.Math.toIntExact;
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
    Integer deletedLineNumber;
    Integer addedLineNumber;
    Integer limit;
    String branchName;
    String vcsUrl;

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

    public int getSize() {
        return this.addedLineNumber + this.deletedLineNumber;
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

    public int getDaysOpened(final Date endDate) {
        if (isNull(this.mergeDate) && isNull(this.closeDate)) {
            return toIntExact(TimeUnit.DAYS.convert(endDate.getTime() - creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        if (isNull(this.mergeDate)) {
            return toIntExact(TimeUnit.DAYS.convert(closeDate.getTime() - this.creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        return toIntExact(TimeUnit.DAYS.convert(mergeDate.getTime() - this.creationDate.getTime(),
                TimeUnit.MILLISECONDS));
    }

    public PullRequestView addTimeLimit() {
        return this.toBuilder().limit(this.getDaysOpened(new Date())).build();
    }

    public PullRequestView addSizeLimit() {
        return this.toBuilder().limit(this.getSize()).build();
    }
}
