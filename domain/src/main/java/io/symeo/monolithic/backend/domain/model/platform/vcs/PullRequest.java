package io.symeo.monolithic.backend.domain.model.platform.vcs;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    String mergeCommitSha;
    @Builder.Default
    Boolean isMerged = false;
    @Builder.Default
    Boolean isDraft = false;
    @NonNull
    Integer number;
    String vcsUrl;
    String title;
    String authorLogin;
    String repository;
    String repositoryId;
    String vcsOrganizationId;
    UUID organizationId;
    String branchName;
    @Builder.Default
    List<Commit> commits = List.of();
    @Builder.Default
    List<Comment> comments = List.of();


    public static String getNameFromRepositoryId(String repositoryId) {
        return ALL + "_" + repositoryId;
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

    public PullRequestView toView() {
        return PullRequestView.builder()
                .addedLineNumber(this.addedLineNumber)
                .deletedLineNumber(this.deletedLineNumber)
                .creationDate(this.creationDate)
                .closeDate(this.closeDate)
                .mergeDate(this.mergeDate)
                .build();
    }

}
