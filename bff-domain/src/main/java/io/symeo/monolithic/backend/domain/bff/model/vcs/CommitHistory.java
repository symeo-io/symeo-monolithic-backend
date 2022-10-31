package io.symeo.monolithic.backend.domain.bff.model.vcs;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Builder
public class CommitHistory {

    private final List<CommitView> commits;
    private final Map<String, CommitView> commitsMappedToSha;

    public static CommitHistory initializeFromCommits(List<CommitView> allCommits) {
        return CommitHistory.builder()
                .commitsMappedToSha(
                        allCommits.stream()
                                .collect(Collectors.toMap(CommitView::getSha, Function.identity()))
                )
                .build();
    }

    public CommitView getCommitFromSha(String mergeCommitSha) {
        return commitsMappedToSha.get(mergeCommitSha);
    }

    public boolean isCommitPresentOnMergeCommitHistory(String pastMergeCommitSha, String futureMergeCommitSha) {
        final CommitView pastCommit = getCommitFromSha(pastMergeCommitSha);
        final CommitView futureCommit = getCommitFromSha(futureMergeCommitSha);
        return isCommitPresentInCommitHistory(pastCommit, futureCommit);
    }

    private boolean isCommitPresentInCommitHistory(CommitView pastCommit, CommitView futureCommit) {
        if (isNull(pastCommit) || isNull(futureCommit)) {
            return false;
        }
        for (CommitView commit : futureCommit.getParentShaList().stream().map(this::getCommitFromSha).toList()) {
            if (commit.getSha().equals(pastCommit.getSha())) {
                return true;
            } else if (!commit.getParentShaList().isEmpty()) {
                return isCommitPresentInCommitHistory(pastCommit, commit);
            }
        }
        return false;
    }

}
