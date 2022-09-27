package io.symeo.monolithic.backend.domain.model.platform.vcs;

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
    String head;
    String repositoryId;

    public static String getNameFromBranch(String branchName) {
        return ALL + "_for_branch_" + branchName.replace("/", "-");
    }
}
