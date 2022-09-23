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
    List<String> parentShaList;
    String head;
    String repositoryId;

    public static String getNameFromPullRequest(final PullRequest pullRequest) {
        return ALL + "_for_pr_number_" + pullRequest.getNumber();
    }

    public static String getNameFromRepository(final Repository repository) {
        return ALL + "_for_repository_" + repository.getId();
    }

}
