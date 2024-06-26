package io.symeo.monolithic.backend.domain.bff.model.vcs;

import io.symeo.monolithic.backend.domain.helper.DateHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@Slf4j
public class PullRequestView {
    public static final List<String> AVAILABLE_SORTING_PARAMETERS = List.of(
            "status", "creation_date", "merge_date", "size", "days_opened", "id", "commit_number", "vcs_url", "title"
            , "author", "vcs_repository"
    );
    private static final String ALL = "pull_requests";
    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String MERGE = "merge";

    String status;
    Date creationDate;
    Date mergeDate;
    Date closeDate;
    String startDateRange;
    Integer deletedLineNumber;
    Integer addedLineNumber;
    Float limit;
    String mergeCommitSha;
    String vcsUrl;
    String authorLogin;
    Integer commitNumber;
    String id;
    String title;
    String repository;
    String base;
    String head;
    Integer number;
    @Builder.Default
    List<CommitView> commits = new ArrayList<>();
    @Builder.Default
    List<CommentView> comments = new ArrayList<>();
    @Builder.Default
    List<String> commitShaList = new ArrayList<>();

    public PullRequestView addStartDateRangeFromRangeDates(final List<Date> rangeDates) {
        String startDateRange;
        if (isNull(mergeDate) && isNull(closeDate)) {
            startDateRange = DateHelper.dateToString(rangeDates.get(rangeDates.size() - 1));
        } else if (nonNull(mergeDate)) {
            Date closestRangeDate = rangeDates.get(0);
            closestRangeDate = getClosestRangeDate(rangeDates, mergeDate, closestRangeDate);
            startDateRange = DateHelper.dateToString(closestRangeDate);
        } else {
            Date closestRangeDate = rangeDates.get(0);
            closestRangeDate = getClosestRangeDate(rangeDates, closeDate, closestRangeDate);
            startDateRange = DateHelper.dateToString(closestRangeDate);
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

    public boolean isAboveTimeLimit(final int pullRequestLimit, final Date weekStartDate, final int range) {
        final ZoneId zoneId = ZoneId.systemDefault();
        final Date now = new Date();
        final long daysBetweenNowAndWeekStartDate =
                TimeUnit.DAYS.convert(now.getTime() - weekStartDate.getTime(),
                        TimeUnit.MILLISECONDS);
        float daysOpened;
        if (daysBetweenNowAndWeekStartDate < range) {
            daysOpened = this.getDaysOpened(now);
        } else {
            daysOpened = this.getDaysOpened(
                    Date.from((weekStartDate.toInstant().atZone(zoneId)
                            .toLocalDate().plusDays(range))
                            .atStartOfDay(zoneId).toInstant()));
        }
        return daysOpened > pullRequestLimit;
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
            return DateHelper.hoursToDays(TimeUnit.HOURS.convert(endDate.getTime() - creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        if (isNull(this.mergeDate)) {
            return DateHelper.hoursToDays(TimeUnit.HOURS.convert(closeDate.getTime() - this.creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        return DateHelper.hoursToDays(TimeUnit.HOURS.convert(mergeDate.getTime() - this.creationDate.getTime(),
                TimeUnit.MILLISECONDS));
    }

    public PullRequestView addTimeLimit() {
        return this.toBuilder().limit(this.getDaysOpened(new Date())).build();
    }

    public PullRequestView addSizeLimit() {
        return this.toBuilder().limit(this.getSize()).build();
    }

    public List<CommitView> getCommitsOrderByDate() {
        List<CommitView> commitArrayList = new ArrayList<>(this.commits);
        commitArrayList = commitArrayList.stream()
                .filter(commit -> {
                    if (isNull(commit)) {
                        LOGGER.warn("Missing commit pour PR {}", this.id);
                        return false;
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());
        commitArrayList.sort(Comparator.comparing(CommitView::getDate));
        return commitArrayList;
    }

    public List<CommentView> getCommentsOrderByDate() {
        final ArrayList<CommentView> commentArrayList = new ArrayList<>(this.comments);
        commentArrayList.sort(Comparator.comparing(CommentView::getCreationDate));
        return commentArrayList;
    }
}
