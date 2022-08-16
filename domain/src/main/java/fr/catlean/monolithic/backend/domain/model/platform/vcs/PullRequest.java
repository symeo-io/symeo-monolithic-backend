package fr.catlean.monolithic.backend.domain.model.platform.vcs;

import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestView;
import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toIntExact;
import static java.util.Objects.isNull;

@Value
@Builder(toBuilder = true)
public class PullRequest {
    private static final String ALL = "pull_requests";
    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String MERGE = "merge";
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
    String repositoryId;
    String vcsOrganizationId;
    UUID organizationId;

    public static String getNameFromRepository(String repositoryName) {
        return ALL + "_" + repositoryName;
    }

    public String getStatus() {
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

    public int getDaysOpened() {
        if (isNull(this.mergeDate) && isNull(this.closeDate)) {
            return toIntExact(TimeUnit.DAYS.convert(new Date().getTime() - creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        if (isNull(this.mergeDate)) {
            return toIntExact(TimeUnit.DAYS.convert(closeDate.getTime() - this.creationDate.getTime(),
                    TimeUnit.MILLISECONDS));
        }
        return toIntExact(TimeUnit.DAYS.convert(mergeDate.getTime() - this.creationDate.getTime(),
                TimeUnit.MILLISECONDS));
    }

    public PullRequestView toSizeLimitView(){
        return PullRequestView.builder()
                .mergeDate(this.mergeDate)
                .limit(this.getSize())
                .closeDate(this.closeDate)
                .status(this.getStatus())
                .creationDate(this.creationDate)
                .build();
    }

    public PullRequestView toTimeLimitView(){
        return PullRequestView.builder()
                .mergeDate(this.mergeDate)
                .limit(this.getDaysOpened())
                .closeDate(this.closeDate)
                .status(this.getStatus())
                .creationDate(this.creationDate)
                .build();
    }


}
