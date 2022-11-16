package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.Builder;

import java.util.HashMap;
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
        return isCommitPresentInCommitHistory(pastCommit, futureCommit, new HashMap<>());
    }

    private boolean isCommitPresentInCommitHistory(final Commit pastCommit,
                                                   final Commit futureCommit,
                                                   final HashMap<String, Boolean> isPresentCached) {
        if (isNull(pastCommit) || isNull(futureCommit)) {
            return false;
        }
        final String key = pastCommit.getSha() + "-" + futureCommit.getSha();
        if (isPresentCached.containsKey(key)) {
            return isPresentCached.get(key);
        }
        for (Commit commit : futureCommit.getParentShaList().stream().map(this::getCommitFromSha).toList()) {
            if (commit.getSha().equals(pastCommit.getSha())) {
                isPresentCached.put(key, true);
                return true;
            } else if (!commit.getParentShaList().isEmpty()) {
                final boolean isPresentInHistory = isCommitPresentInCommitHistory(pastCommit, commit, isPresentCached);
                if (isPresentInHistory) {
                    return true;
                }
            }
        }
        isPresentCached.put(key, false);
        return false;
    }
}
