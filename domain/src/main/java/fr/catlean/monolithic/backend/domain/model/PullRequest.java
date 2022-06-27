package fr.catlean.monolithic.backend.domain.model;

import lombok.Builder;
import lombok.Value;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Value
@Builder(toBuilder = true)
public class PullRequest {
    private static final String ALL = "pull_requests";
    private static final String OPEN = "open";
    private static final String CLOSE = "close";
    private static final String MERGE = "merge";
    String id;
    Integer commitNumber;
    Integer deletedLineNumber;
    Integer addedLineNumber;
    Date creationDate;
    Date lastUpdateDate;
    Date mergeDate;
    Date closeDate;
    @Builder.Default
    Boolean isMerged = false;
    @Builder.Default
    Boolean isDraft = false;
    int number;
    String vcsUrl;
    String title;
    String authorLogin;
    String repository;
    String vcsOrganization;
    String organization;
    String team;

    public static String getNameFromRepository(String repositoryName) {
        return ALL + "_" + repositoryName;
    }

    public String getState() {
        if (isNull(this.closeDate) && isNull(this.mergeDate)) {
            return OPEN;
        }
        if (isNull(this.mergeDate)) {
            return CLOSE;
        }
        return MERGE;
    }

    public int getSize() {
        return this.addedLineNumber + this.deletedLineNumber;
    }

    public long getDaysOpened() {
        if (isNull(this.mergeDate) && isNull(this.closeDate)) {
            return TimeUnit.DAYS.convert(new Date().getTime() - creationDate.getTime(),
                    TimeUnit.MILLISECONDS);
        }
        if (isNull(this.mergeDate)) {
            return TimeUnit.DAYS.convert(closeDate.getTime() - this.creationDate.getTime(),
                    TimeUnit.MILLISECONDS);
        }
        return TimeUnit.DAYS.convert(mergeDate.getTime() - this.creationDate.getTime(),
                TimeUnit.MILLISECONDS);
    }

    public String getStartDateRange() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        if (isNull(this.mergeDate) && isNull(this.closeDate)) {
            return simpleDateFormat.format(new Date());
        }
        if (nonNull(this.mergeDate)) {
            return simpleDateFormat.format(this.mergeDate);
        }
        return simpleDateFormat.format(this.closeDate);
    }

    public boolean isConsideredAsOpenDuringWeek(final Date weekStartDate) {
        final Date creationDate = this.getCreationDate();
        final Date mergeDate = this.getMergeDate();
        if (creationDate.before(weekStartDate)) {
            return isNull(mergeDate) || mergeDate.after(weekStartDate);
        } else {
            if (creationDate.equals(weekStartDate)) {
                return true;
            } else if (nonNull(mergeDate)) {
                long daysBetweenMergeAndWeekStart =
                        TimeUnit.DAYS.convert(mergeDate.getTime() - weekStartDate.getTime(),
                                TimeUnit.MILLISECONDS);
                long daysBetweenCreationAndWeekStart =
                        TimeUnit.DAYS.convert(creationDate.getTime() - weekStartDate.getTime(),
                                TimeUnit.MILLISECONDS);
                return daysBetweenCreationAndWeekStart < 7 && daysBetweenMergeAndWeekStart < 7;
            }
        }
        return false;
    }

    public boolean isValid() {
        return !this.getIsDraft() && !(isNull(this.getMergeDate()) && nonNull(this.getCloseDate()));
    }
}
