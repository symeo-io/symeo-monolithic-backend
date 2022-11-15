package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Builder
public class CommitHistory {
    private final List<Commit> commits;
    private final Map<String, Commit> commitsMappedToSha;

    public static CommitHistory initializeFromCommits(List<Commit> allCommits) {
        return CommitHistory.builder()
                .commitsMappedToSha(
                        allCommits.stream()
                                .collect(Collectors.toMap(Commit::getSha, Function.identity()))
                )
                .build();
    }

    public Commit getCommitFromSha(String mergeCommitSha) {
        return commitsMappedToSha.get(mergeCommitSha);
    }

    public boolean isCommitPresentOnMergeCommitHistory(String pastMergeCommitSha, String futureMergeCommitSha) {
        final Commit pastCommit = getCommitFromSha(pastMergeCommitSha);
        final Commit futureCommit = getCommitFromSha(futureMergeCommitSha);
        return isCommitPresentInCommitHistory(pastCommit, futureCommit);
    }

    private boolean isCommitPresentInCommitHistory(Commit pastCommit, Commit futureCommit) {
        if (isNull(pastCommit) || isNull(futureCommit)) {
            return false;
        }
        for (Commit commit : futureCommit.getParentShaList().stream().map(this::getCommitFromSha).toList()) {
            if (commit.getSha().equals(pastCommit.getSha())) {
                return true;
            } else if (!commit.getParentShaList().isEmpty()) {
                final boolean isPresentInHistory = isCommitPresentInCommitHistory(pastCommit, commit);
                if (isPresentInHistory){
                    return true;
                }
            }
        }
        return false;
    }
}
