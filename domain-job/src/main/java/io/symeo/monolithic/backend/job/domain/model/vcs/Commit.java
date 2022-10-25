package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class Commit {
    private static final String ALL = "commits";
    String author;
    Date date;
    String message;
    String sha;
    @Builder.Default
    List<String> parentShaList = List.of();
    String repositoryId;

    public static String getNameFromBranchAndRepository(String branchName, Repository repository) {
        return ALL + "_for_repository_" + repository.getId() + "_and_branch_" + branchName.replace("/", "-");
    }
}
