package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@Slf4j
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
    String head;
    String base;
    @Builder.Default
    List<Commit> commits = new ArrayList<>();
    @Builder.Default
    List<Comment> comments = new ArrayList<>();
    @Builder.Default
    List<String> commitShaList = new ArrayList<>();

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

    public List<Commit> getCommitsOrderByDate() {
        List<Commit> commitArrayList = new ArrayList<>(this.commits);
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
        commitArrayList.sort(Comparator.comparing(Commit::getDate));
        return commitArrayList;
    }

    public List<Comment> getCommentsOrderByDate() {
        final ArrayList<Comment> commentArrayList = new ArrayList<>(this.comments);
        commentArrayList.sort(Comparator.comparing(Comment::getCreationDate));
        return commentArrayList;
    }

}
