package io.symeo.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public class CommitHistory {

    private final List<Commit> commits;
    private final Map<String, Commit> commitsMappedToSha;
    private final List<String> allBranches;

    public static CommitHistory initializeFromCommits(List<Commit> allCommits) {
        return CommitHistory.builder()
                .commitsMappedToSha(
                        allCommits.stream()
                                .collect(Collectors.toMap(Commit::getSha, Function.identity()))
                )
                .allBranches(
                        allCommits.stream()
                                .map(Commit::getHead)
                                .filter(Objects::nonNull)
                                .toList())
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
        for (Commit commit : futureCommit.getParentShaList().stream().map(this::getCommitFromSha).toList()) {
            if (commit.getSha().equals(pastCommit.getSha())) {
                return true;
            } else if (!commit.getParentShaList().isEmpty()) {
                return isCommitPresentInCommitHistory(pastCommit, commit);
            }
        }
        return false;
    }
}
