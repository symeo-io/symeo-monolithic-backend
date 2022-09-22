package io.symeo.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;

import java.util.Date;
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

    public Date getMergeDateForCommitOnBranch(final Commit mergeCommit, final String matchedBranch) {
        Commit currentCommit = commits.stream()
                .filter(commit -> commit.getHead().equals(matchedBranch))
                .findFirst()
                .orElseThrow(RuntimeException::new);
        for (String parentSha : currentCommit.getParentShaList()) {
            Commit parentCommit = getCommitFromSha(parentSha);

        }
        return null;
    }

    public List<String> getAllBranches() {
        return this.allBranches;
    }
}
